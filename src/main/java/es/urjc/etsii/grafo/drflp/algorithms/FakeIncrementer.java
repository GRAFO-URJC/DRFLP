package es.urjc.etsii.grafo.drflp.algorithms;

import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.services.Global;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.logging.Logger;

/**
 * Repeatedly calls a given algorithm incrementing the set of fake facilities until a threshold is reached.
 * The increment, threshold etc are all configurable.
 */
public class FakeIncrementer extends Algorithm<DRFLPSolution, DRFLPInstance> {

    private static final Logger log = Logger.getLogger(FakeIncrementer.class.getName());

    /**
     * Specifies how to calculate number of fake facilities for a given iteration
     */
    private final IntUnaryOperator getNFakes;

    /**
     * Specifies how to calculate the stop threshold
     */
    private final Function<DRFLPInstance, Integer> thCalculator;

    /**
     * Algorithm to execute in each iteration
     */
    private final AlgorithmProvider algorithmProvider;

    /**
     * Current algorithm name
     */
    private final String algName;

    /**
     * Maximum time budget
     */
    private final long timeBudget;


    @Override
    public String getShortName() {
        if(algName != null && !algName.isBlank()){
            return algName;
        }
        return super.getShortName();
    }

    @Deprecated
    public FakeIncrementer(String name, AlgorithmProvider constructor) {
        this(name, constructor, new FIUtil.FibonacciFakesForIteration(), new FIUtil.FractionOfWidthStop(0.1D), 100, TimeUnit.DAYS);
    }

    public FakeIncrementer(String name, AlgorithmProvider constructor, IntUnaryOperator getNFakes, Function<DRFLPInstance, Integer> thCalculator, int timeLimit, TimeUnit unit) {
        this.getNFakes = getNFakes;
        this.algName = name;
        this.algorithmProvider = constructor;
        this.thCalculator = thCalculator;
        this.timeBudget = unit.toNanos(timeLimit);
    }


    private Algorithm<DRFLPSolution, DRFLPInstance> getAlgorithm(int n) {
        double[] fakes = new double[n];
        for (int i = 0; i < n; i++) {
            fakes[i] = 0.5D;
        }

        var alg = this.algorithmProvider.build(fakes);
        alg.setBuilder(this.getBuilder());
        return alg;
    }

    @Override
    public DRFLPSolution algorithm(DRFLPInstance instance) {
        long start = System.nanoTime();
        final AtomicBoolean alreadyFinished = new AtomicBoolean(false);

        // This thread monitors elapsed time and stops main thread if the time budget is exhausted
        new Thread(()->{
            while(System.nanoTime() - start < timeBudget){
                ConcurrencyUtil.sleep(100, TimeUnit.MILLISECONDS);
            }

            synchronized (this){
                if(!alreadyFinished.get()){
                    Global.setStop(true);
                }
            }
        }).start();

        int threshold = thCalculator.apply(instance);
        int currentIndex = 0;
        int lastImprovementWithNFakes = 0;
        DRFLPSolution best = null;

        log.info(String.format("Start th=%s, self: %s", threshold, this));
        while (true) {
            long iterationStart = System.nanoTime();
            int nFakes = getNFakes.applyAsInt(currentIndex);
            var alg = getAlgorithm(nFakes);
            var solution = alg.algorithm(instance);
            if (best == null || DoubleComparator.isLessThan(solution.getScore(), best.getScore())) {
                best = solution;
                lastImprovementWithNFakes = nFakes;
            }
            long iterationEnd = System.nanoTime();
            log.info(String.format("Fakes: %s, T(s): %s, best: %s, score: %s", nFakes, (iterationEnd - iterationStart)/1_000_000_000d, best.getScore(), solution.getScore()));
            if (nFakes - lastImprovementWithNFakes > threshold) {
                log.info(String.format("Done. Last improve at %s fakes, current nFakes %s", lastImprovementWithNFakes, nFakes));
                break;
            }
            if(iterationEnd - start > timeBudget){
                log.info(String.format("Done, time budget consumed. Last improve at %s fakes, current nFakes %s", lastImprovementWithNFakes, nFakes));
                break;
            }
            currentIndex++;
        }
        synchronized (this){
            alreadyFinished.set(true);
            Global.setStop(false);
        }

        return best;
    }



    @Override
    public String toString() {
        return "FI{" +
                "a=" + algorithmProvider.build(new double[0]) +
                '}';
    }
}
