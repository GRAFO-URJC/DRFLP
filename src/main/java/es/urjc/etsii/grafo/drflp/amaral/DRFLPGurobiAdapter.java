package es.urjc.etsii.grafo.drflp.amaral;

import es.urjc.etsii.grafo.drflp.constructives.grasp.DRFPAddMove;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.drflp.model.Facility;
import es.urjc.etsii.grafo.util.DoubleComparator;
import gurobi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static gurobi.GRB.INFINITY;

/**
 * Gurobi adapter to solve models from Java
 */
public class DRFLPGurobiAdapter {
    private static final Logger log = LoggerFactory.getLogger(DRFLPGurobiAdapter.class);

    /**
     * Stop after one hour by default
     */
    public static final int DEFAULT_TIME_LIMIT = 3600;

    /**
     * Gurobi environment
     */
    protected final GRBEnv env;

    public DRFLPGurobiAdapter() {
        try {
            this.env = initializeEnvironment(DEFAULT_TIME_LIMIT);
        } catch (GRBException e){
            throw new RuntimeException(e);
        }
    }

    protected static DRFLPSolution.RowIndex getRowIndex(Facility[][] data, int position) {
        int row = 0;
        while (position >= data[row].length) {
            position -= data[row].length;
            row++;
        }
        return new DRFLPSolution.RowIndex(row, position);
    }

    protected RuntimeException onModelFail(AmaralSolution solution, GRBModel model, int status) {
        log.error("Gurobi fail: {}", status);
        return new RuntimeException("Failed gurobi solve");
    }

    protected DRFLPSolution onModelSolved(AmaralSolution solution, GRBModel model, GRBVar[][] centerVars) throws GRBException {
        double of = model.get(GRB.DoubleAttr.ObjVal);
        log.debug("GurobiLS Improvement: {}, ({}  - {})", solution.v - of, solution.v, of);
        var centers = toCenters(centerVars);
        var instance = solution.instance;

        List<FakeFacilityData> fakeFacilityDataList = new ArrayList<>();
        var result = solution.transform();

        // Compare Gurobi calculated centers against real solution real data
        // Create fake facilities where there is a gap
        for (int i = 0; i < centers.length; i++) {
            double displacement = 0;
            int position = 0;

            var sol_row = result.getSolutionData()[i];
            var gurobi_center = centers[i];
            for (int j = 0; j < centers[i].length; j++) {
                double difference = gurobi_center[j] - sol_row[j].lastCenter - displacement;
                if (DoubleComparator.isPositive(difference)) {
                    displacement += difference;
                    long nFakes = Math.round(difference * 2);
                    for (int k = 0; k < nFakes; k++) {
                        fakeFacilityDataList.add(new FakeFacilityData(0.5, position++, i));
                    }
                }
                position++;
            }
        }


        // Initialize new fake facilities in solution
        var pendingFakeFacilities = result.addFakeFacilities(fakeFacilityDataList.stream().mapToDouble(d -> d.width).toArray());

        // Add fake facilities in positions suggested by Gurobi
        for (int i = 0; i < fakeFacilityDataList.size(); i++) {
            FakeFacilityData f = fakeFacilityDataList.get(i);
            var addMove = new DRFPAddMove(result, f.row, f.index, pendingFakeFacilities[i]);
            addMove.execute();
        }

        // Validation: Gurobi score should match my calculation or something went wrong
        assert DoubleComparator.equals(result.getScore(), of);

        return result;
    }

    protected double[][] toCenters(GRBVar[][] centerVars) throws GRBException {
        double[][] centers = new double[centerVars.length][];
        for (int i = 0; i < centerVars.length; i++) {
            centers[i] = new double[centerVars[i].length];
            for (int j = 0; j < centerVars[i].length; j++) {
                centers[i][j] = centerVars[i][j].get(GRB.DoubleAttr.X);
            }
        }
        return centers;
    }

    protected GRBVar[][] initializeModel(GRBModel model, AmaralSolution solution) throws GRBException {
        var facilities = toFacilityMatrix(solution);
        var instance = solution.instance;

        int nN = instance.getNRealFacilities();

        var xm = new GRBVar[nN];
        for (int i = 0; i < nN; i++) {
            // lowerbound, upperbound, obj.function multiplier, continous/discrete, name
            xm[i] = model.addVar(-INFINITY, INFINITY, 0.0, GRB.CONTINUOUS, "Fpos_" + i);
        }

        int dm_length = nN * (nN - 1) / 2;
        var dm = new GRBVar[dm_length];
        for (int i = 0; i < dm_length; i++) {
            dm[i] = model.addVar(-INFINITY, INFINITY, 0.0, GRB.CONTINUOUS, "Dist_" + i);
        }

        // Set objective and abs constraints
        int uv = 0;
        var objective = new GRBLinExpr();
        for (int u = 0; u < nN - 1; u++) {
            for (int v = u + 1; v < nN; v++) {
                GRBLinExpr t1 = new GRBLinExpr(), t2 = new GRBLinExpr();
                t1.addTerm(1, xm[u]); t1.addTerm(-1, xm[v]);
                t2.addTerm(1, xm[v]); t2.addTerm(-1, xm[u]);
                model.addConstr(dm[uv], GRB.GREATER_EQUAL, t1, "abs1-%s-%s".formatted(u, v));
                model.addConstr(dm[uv], GRB.GREATER_EQUAL, t2, "abs1-%s-%s".formatted(u, v));
                objective.addTerm(instance.getWeight(u, v), dm[uv]);
                uv++;
            }
        }

        model.setObjective(objective, GRB.MINIMIZE);

        // Add constraints
        for (int r = 0; r < facilities.length; r++) {
            var first = facilities[r][0];
            model.addConstr(xm[first.id], GRB.GREATER_EQUAL, first.width / 2, "overlapfirst-%s".formatted(r));
            for (int i = 0; i < facilities[r].length - 1; i++) {
                Facility u = facilities[r][i];
                Facility v = facilities[r][i + 1];
                var rightHand = new GRBLinExpr();
                rightHand.addTerm(1, xm[u.id]);
                rightHand.addConstant((u.width + v.width) / 2.0);
                model.addConstr(xm[v.id], GRB.GREATER_EQUAL, rightHand, "overlap-%s-%s".formatted(r, i));
            }
        }

        model.setObjective(objective, GRB.MINIMIZE);

        // Transform array of variables to matrix with the correct positions
        var vars = new GRBVar[][]{
                new GRBVar[solution.t],
                new GRBVar[nN - solution.t]
        };

        // First row
        for (int i = 0; i < solution.t; i++) {
            vars[0][i] = xm[solution.pi[i]];
        }
        // Second row
        for (int i = solution.t; i < solution.pi.length; i++) {
            vars[1][i - solution.t] = xm[solution.pi[i]];
        }
        return vars;
    }

    protected Facility[][] toFacilityMatrix(AmaralSolution solution) {
        var data = solution.toMatrix();
        var instance = solution.instance;
        var result = new Facility[data.length][];
        for (int i = 0; i < data.length; i++) {
            result[i] = new Facility[data[i].length];
            for (int j = 0; j < data[i].length; j++) {
                result[i][j] = instance.byId(data[i][j]);
            }
        }
        return result;
    }

    protected GRBEnv initializeEnvironment(int timeLimitInSeconds) throws GRBException {
        var env = new GRBEnv("gurobi_log.txt");
        env.set(GRB.DoubleParam.TimeLimit, timeLimitInSeconds);
        env.set(GRB.IntParam.LogToConsole, 0);
        env.set(GRB.IntParam.OutputFlag, 0);
        env.start();
        return env;
    }

    public DRFLPSolution execute(AmaralSolution solution) {
        try {
            var model = new GRBModel(this.env);
            var centers = initializeModel(model, solution);
            model.optimize();

            int status = model.get(GRB.IntAttr.Status);
            if (status != GRB.INFEASIBLE) {
                var result = onModelSolved(solution, model, centers);
                return result;
            }
            throw onModelFail(solution, model, status);
        } catch (GRBException e) {
            throw new RuntimeException(e);
        } finally {
            if(env != null){
                try {
                    env.dispose();
                } catch (GRBException ignored) {}
            }
        }
    }

    protected record FakeFacilityData(double width, int index, int row) {}
}