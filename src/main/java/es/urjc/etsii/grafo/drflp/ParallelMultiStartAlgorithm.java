package es.urjc.etsii.grafo.drflp;

import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.random.RandomType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class ParallelMultiStartAlgorithm extends Algorithm<DRFLPSolution, DRFLPInstance>{
    private static Logger log = Logger.getLogger(ParallelMultiStartAlgorithm.class.getName());
    private final int n;

    private final ExecutorService executor;
    private final Algorithm<DRFLPSolution, DRFLPInstance> alg;

    public ParallelMultiStartAlgorithm(int n, String name, Algorithm<DRFLPSolution, DRFLPInstance> alg) {
        this.n = n;
        this.alg = alg;
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Algorithm: Execute a single construction and then all the local searchs N times, returning best solution of all iterations.
     * @param instance Instance
     * @return Returns a valid solution
     */
    @Override
    public DRFLPSolution algorithm(DRFLPInstance instance) {
        try {
            List<Future<DRFLPSolution>> futures = new ArrayList<>();
            RandomManager.reinitialize(RandomType.LEGACY, 0, this.n);
            for (int i = 0; i < n; i++) {
                int _i = i;
                futures.add(executor.submit(() -> {
                    RandomManager.reset(_i);
                    return alg.algorithm(instance);
                }));
            }
            var results = ConcurrencyUtil.awaitAll(futures);
            DRFLPSolution best = null;
            for(var solution: results){
                if(solution.isBetterThan(best)){
                    best = solution;
                }
            }
            return best;
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public String toString() {
        return "PMS{" +
                "n=" + n +
                ", alg=" + alg +
                '}';
    }

    @Override
    public void setBuilder(SolutionBuilder<DRFLPSolution, DRFLPInstance> builder) {
        super.setBuilder(builder);
        this.alg.setBuilder(builder);
    }
}
