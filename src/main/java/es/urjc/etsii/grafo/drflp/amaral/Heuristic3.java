package es.urjc.etsii.grafo.drflp.amaral;

import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static es.urjc.etsii.grafo.util.DoubleComparator.isLessThan;

public class Heuristic3 extends AbstractAmaralHeuristic{
    private static final Logger log = Logger.getLogger(Heuristic3.class.getName());

    public Heuristic3(DRFLPGurobiAdapter gurobi, int time, TimeUnit unit) {
        super(gurobi, time, unit);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected AmaralSolution phase1(int T1, int T2, int ITER, int MAX_k, AmaralSolution sol) {
        var rand = RandomManager.getRandom();
        long startTime = System.nanoTime();
        int n = sol.pi().length;
        AmaralSolution bestSolution = null;


        double bestV = Double.MAX_VALUE;                                    // v*  ← INFINITY

        sol.t = n / 2;                                                      // t ← n2;
        amaralShuffle(sol.pi());                                            // Shuffle(π, n);
        for (int iter = 0; iter < ITER; iter++) {                           // for iter ← 1 to ITER do
            inversion(sol); sol.r = 0; sol.s = 0;                           //   Inversion(π, n); r ← 0; s ← 0;
            for (int k = 0; k < MAX_k; k++) {                               //   for k ← 1 to M A X _k do
                AmaralSolution temp = twoOPT_LS(sol);                       //     [π, v] ← 2OPT_LS(π, t, r, s);

                if(isLessThan(temp.v, bestV)){                                         //     if v < v∗ then
                    bestV = temp.v;                                         //       v∗ ← v;
                    bestSolution = temp;                                    //       (π∗, t∗, r∗, s∗) ← (π, t, r, s);
                }                                                           //     end
                sol.r = sol.r + (0.5) * rand.nextInt(-1, 2);   //     r ← r + (0.5) × Random(−1, +1);
                sol.s = sol.s + (0.5) * rand.nextInt(-1, 2);   //     s ← s + (0.5) × Random(−1, +1) ;
                sol.t = sol.t + rand.nextInt(-1, 2);           //     t ← t + Random(−1,+1);
                if(sol.t < T1 || sol.t > T2){                               //     if t < T1 or t > T2 then
                    sol.t = n / 2;                                          //       t ← n / 2;
                }                                                           //     end
                if (timeBudgetConsumed(startTime)) {
                    return bestSolution;
                }
            }                                                               //   end
        }                                                                   // end
        return bestSolution; // return π∗, t∗, r∗, s∗;
    }
}
