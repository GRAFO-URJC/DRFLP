package es.urjc.etsii.grafo.drflp.amaral;

import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.DoubleComparator.isLessThan;

public abstract class AbstractAmaralHeuristic extends Algorithm<DRFLPSolution, DRFLPInstance> {

    private static final Logger log = Logger.getLogger(AbstractAmaralHeuristic.class.getName());
    private final DRFLPGurobiAdapter gurobi;
    protected final long nanoBudget;

    protected AbstractAmaralHeuristic(DRFLPGurobiAdapter gurobi, int timeLimit, TimeUnit unit) {
        this.nanoBudget = unit.toNanos(timeLimit);
        this.gurobi = gurobi;
    }

    /**
     * Calculate T1 parameter as in Amaral 2020
     * T1 = ⌊n / 2⌋
     * @param instance DRFP Instance currently being solved
     * @return T1 parameter for the algorithm
     */
    public int T1(DRFLPInstance instance){
        int n = instance.getNRealFacilities();
        return n / 2; // Floor not necessary, integer division ignores remainder
    }

    /**
     * Calculate T2 parameter as in Amaral 2020
     * T2 = ⌊n / 2⌋ + 4
     * @param instance DRFP Instance currently being solved
     * @return T2 parameter for the algorithm
     */
    public int T2(DRFLPInstance instance){
        int n = instance.getNRealFacilities();
        return n / 2 + 4; // Floor not necessary, integer division ignores remainder
    }

    /**
     * Calculate ITER parameter as in Amaral 2020
     * ITER = MAX_k = 200 (if n < 50)
     * ITER = MAX_k = 30 (if n == 50)
     * @param instance DRFP Instance currently being solved
     * @return ITER parameter for the algorithm
     */
    public int ITER(DRFLPInstance instance){
        return MAX_k(instance);
    }

    /**
     * Calculate MAX_k parameter as in Amaral 2020
     * ITER = MAX_k = 200 (if n < 50)
     * ITER = MAX_k = 30 (if n == 50)
     * @param instance DRFP Instance currently being solved
     * @return MAX_k parameter for the algorithm
     */
    public int MAX_k(DRFLPInstance instance){
        return switch (instance.getNRealFacilities()){
            case 50 -> 30;  // if n == 50, ITER = MAX_k = 30
            default -> 200; // else ITER = MAX_k = 200
        };
    }

    @Override
    public DRFLPSolution algorithm(DRFLPInstance instance) {
        // Calculate algorithm parameters for current instance
        int T1 = T1(instance), T2 = T2(instance), ITER = ITER(instance), MAX_k = MAX_k(instance);

        // Initialize solution as [0,1,2,3...N-1]. Facilities are 0 indexed
        AmaralSolution solution = initializeSolution(instance);

        // Run the heuristic (phase 1)
        solution = phase1(T1, T2, ITER, MAX_k, solution);
        double scoreAfterHeuristic = solution.v;

        // Run the Gurobi solver (phase 2)
        DRFLPSolution finalsolution = phase2(solution);
        double scoreAfterGurobi = finalsolution.getScore();

        // Solution can never be worse after executing Gurobi
        assert DoubleComparator.isLessOrEquals(scoreAfterGurobi, scoreAfterHeuristic);

        // Return solution using my model to draw it and extract metrics
        return finalsolution;
    }


    protected AmaralSolution initializeSolution(DRFLPInstance instance) {
        int[] pi = new int[instance.getNRealFacilities()];
        for (int i = 0; i < instance.getNRealFacilities(); i++) {
            pi[i] = i;
        }

        return new AmaralSolution(pi, instance);
    }

    // BUG WARNING: This shuffle is not equiprobable, should be using Fisher-Yates or equivalent instead
    protected void amaralShuffle(int[] data){
        var random = RandomManager.getRandom();

        for (int i = 0; i < data.length; i++) {
            int j = random.nextInt(0, data.length);
            // Swap
            int temp = data[i];
            data[i] = data[j];
            data[j] = temp;
        }
    }

    /**
     * Corresponds with Algorithm 1 in Amaral2020. Implements a swap move, not 2Opt despite the name
     * @param solution original solution
     * @return modified solution
     */
    protected AmaralSolution twoOPT_LS(AmaralSolution solution){
        int n = solution.instance.getNRealFacilities();

        double v = solution.v; // v ← f(π,t,r,s); (cached for performance)
        int key;
        do {                                        // repeat
            key = 1;                                // key ← 1;
            outerFor:
            for (int i = 0; i < n - 1; i++) {       // for i ← 1 to n − 1 do
                for (int j = i + 1; j < n; j++) {   // for j ← i + 1 to n do
                    AmaralSolution copy = solution.copy();  // if swapping the facilities at positions i and j of π under parameter values t , r , s , produces a layout(π,t,r,s) such that, f(π,t,r,s) < v then
                    ArrayUtil.swap(copy.pi, i, j);
                    copy.updateScore();
                    if(isLessThan(copy.v, v)){
                        v = copy.v;         // v ← f(π,t,r,s);
                        solution = copy;    // π ← π with hat;
                        key = 0;            // key ← 0;
                        break outerFor;     // go to label_1;
                    }
                }
            }
        } while (key != 1);    // until key = 1;

        return solution;       // return π, v;
    }

    /**
     * Corresponds with Algorithm 3 in Amaral2020. Implements a 2Opt move
     * @param solution original solution
     * @return modified solution
     */
    protected AmaralSolution inversion(AmaralSolution solution){
        var random = RandomManager.getRandom();
        var pi = solution.pi;
        int n = pi.length;

        int i = random.nextInt(0, n);                         // i ← Random(1, n);
        int q = n >= 20 ?                                           // if n ≥ 20 then
                random.nextInt(1 + n / 8, n / 4 + 1):  //    q = Random(1 + ⌊n / 8⌋, ⌊n / 4⌋);
                                                                    // else
                random.nextInt(3, 5);                  //    q = Random(3, 4);
        int j = i + q - 1;                                          // j = i + q − 1;
        if (j >= n){                                                // if j > n then
            j = j - n;                                              //     j ← j − n;
        }                                                           // end

        for (int k = 0; k < q/2; k++) {                             // for k ← 1 to ⌊q2⌋ do
            ArrayUtil.swap(pi, i, j);                               // Swap the machines at positions i and j of π ;
            i = i + 1;                                              // i ← i + 1;
            if(i == n){                                             // if i = n + 1 then
                i = 0;                                              //     i ← 1;
            }                                                       // end
            j = j - 1;                                              // j ← j − 1;
            if(j == -1){                                            // if j = 0 then
                j = n - 1;                                          //     j ← n;
            }                                                       // end
        }
        return solution;
    }

    /**
     * To be implemented by the different heuristic methods
     * @return solution generated after phase1
     */
    protected abstract AmaralSolution phase1(int T1, int T2, int ITER, int MAX_k, AmaralSolution solution);

    protected DRFLPSolution phase2(AmaralSolution solution){
        return gurobi.execute(solution);
    }

    @Override
    public String getShortName() {
        return this.getClass().getSimpleName() + "_" + this.gurobi.getClass().getSimpleName().replace("Adapter", "");
    }

    protected boolean timeBudgetConsumed(long startTime){
        return System.nanoTime() - startTime > this.nanoBudget;
    }
}
