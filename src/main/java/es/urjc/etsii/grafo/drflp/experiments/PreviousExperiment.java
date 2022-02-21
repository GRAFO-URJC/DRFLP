package es.urjc.etsii.grafo.drflp.experiments;

import es.urjc.etsii.grafo.drflp.amaral.*;
import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PreviousExperiment extends AbstractExperiment<DRFLPSolution, DRFLPInstance> {

    protected PreviousExperiment(SolverConfig config) {
        super(config);
    }

    @Override
    public List<Algorithm<DRFLPSolution, DRFLPInstance>> getAlgorithms() {
        var gurobi = new DRFLPGurobiAdapter();
        return Arrays.asList(
                new Heuristic1(gurobi, 1, TimeUnit.HOURS),
                new Heuristic2(gurobi, 1, TimeUnit.HOURS),
                new Heuristic3(gurobi, 1, TimeUnit.HOURS),
                new Heuristic4(gurobi, 1, TimeUnit.HOURS)
        );
    }
}
