package es.urjc.etsii.grafo.drflp.amaral;

import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.DoubleComparator.isLessThan;

public class Heuristic2 extends AbstractAmaralHeuristic{

    private static final Logger log = Logger.getLogger(Heuristic2.class.getName());

    public Heuristic2(DRFLPGurobiAdapter gurobi, int time, TimeUnit unit) {
        super(gurobi, time, unit);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected AmaralSolution phase1(int T1, int T2, int ITER, int MAX_k, AmaralSolution sol) {
        var rand = RandomManager.getRandom();
        long startTime = System.nanoTime();
        AmaralSolution bestSolution = null;

        double bestV = Double.MAX_VALUE;                                        // v*  ← INFINITY

        for (int t = T1; t < T2; t++) {                                         // for t ← T1 to T2 do
            sol.t = t;
            amaralShuffle(sol.pi());                                            //   Shuffle(π, n);
            for (int iter = 0; iter < ITER; iter++) {                           //   for iter ← 1 to ITER do
                inversion(sol); sol.r = 0; sol.s = 0;                           //     Inversion(π, n); r ← 0; s ← 0;
                for (int k = 0; k < MAX_k; k++) {                               //     for k ← 1 to M A X _k do
                    AmaralSolution temp = twoOPT_LS(sol);                       //       [π, v] ← 2OPT_LS(π, t, r, s);

                    if(isLessThan(temp.v, bestV)){                                         //       if v < v∗ then
                        bestV = temp.v;                                         //         v∗ ← v;
                        bestSolution = temp;                                    //         (π∗, t∗, r∗, s∗) ← (π, t, r, s);
                    }                                                           //       end
                    sol.r = sol.r + (0.5) * rand.nextInt(-1, 2);   //       r ← r + (0.5) × Random(−1, +1);
                    sol.s = sol.s + (0.5) * rand.nextInt(-1, 2);   //       s ← s + (0.5) × Random(−1, +1) ;
                    if (timeBudgetConsumed(startTime)) {
                        return bestSolution;
                    }
                }                                                               //     end
            }                                                                   //   end
        }                                                                       // end
        return bestSolution; // return π∗, t∗, r∗, s∗;
    }
}
