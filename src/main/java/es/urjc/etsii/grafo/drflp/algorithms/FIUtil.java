package es.urjc.etsii.grafo.drflp.algorithms;

import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;

import java.util.function.Function;
import java.util.function.IntUnaryOperator;

/**
 * Fake incrementer util configuration
 */
public class FIUtil {

    // STRATEGIES TO DEFINE HOW BIG THE FAKES INCREASE MUST BE BEFORE STOPPING
    public record FractionOfWidthStop(double fraction) implements Function<DRFLPInstance, Integer> {
        /**
         * Stops when currentFakes - lastImprovementAtNFakes &gt; totalWidth * fraction
         * @param instance instance being solved
         * @return max difference between currentFakes and lastImprovementAtNFakes
         */
        @Override
        public Integer apply(DRFLPInstance instance) {
            return Math.toIntExact(Math.round(instance.getTotalWidth() * fraction));
        }
    }

    public record ConstantStop(int constant) implements Function<DRFLPInstance, Integer> {
        /**
         * Stops when currentFakes - lastImprovementAtNFakes &gt; constant
         * @param instance instance being solved
         * @return max difference between currentFakes and lastImprovementAtNFakes
         */
        @Override
        public Integer apply(DRFLPInstance instance) {
            return constant;
        }
    }

    // Example: Stop when added fakes facilities since last improvement is greater than a tenth of total instance width
    private static final Function<DRFLPInstance, Integer> oneTenthOfTotalWidth = new FractionOfWidthStop(0.1D);
    // Example: Stop when we have added at least 5 fake facilities and no improvement has been made
    private static final Function<DRFLPInstance, Integer> fiveFakesAndNoImprovement = new ConstantStop(5);
    // Example: Stop when added fakes facilities since last improvement is greater than sqrt(total instance width)
    private static final Function<DRFLPInstance, Integer> sqrtTotalWidth = instance -> Math.toIntExact(Math.round(Math.sqrt(instance.getTotalWidth())));

    // STRATEGIES TO DEFINE HOW MANY FAKE FACILITIES SHOULD WE TEST IN EACH ITERATION
    public record LinearFakesForIteration(int ratio) implements IntUnaryOperator {
        /**
         * calculate fake facilities as constant * currentIteration
         * @param currentIteration currentIteration
         * @return fakes facilities to use for current iteration
         */
        @Override
        public int applyAsInt(int currentIteration) {
            return ratio * currentIteration;
        }
    }

    public record FibonacciFakesForIteration() implements IntUnaryOperator {
        /**
         * Calculate fake facilities as the corresponding number in the fibonacci sequence
         * @param currentIteration currentIteration
         * @return fakes facilities to use for current iteration
         */
        @Override
        public int applyAsInt(int currentIteration) {
            if (currentIteration <= 1) {
                return currentIteration;
            }
            int fib = 1;
            int prevFib = 1;

            for (int i = 2; i <= currentIteration; i++) {
                int temp = fib;
                fib += prevFib;
                prevFib = temp;
            }
            return fib;
        }
    }
}
