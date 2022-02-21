package es.urjc.etsii.grafo.drflp.experiments;

import es.urjc.etsii.grafo.drflp.ParallelMultiStartAlgorithm;
import es.urjc.etsii.grafo.drflp.algorithms.FIUtil;
import es.urjc.etsii.grafo.drflp.algorithms.FakeIncrementer;
import es.urjc.etsii.grafo.drflp.constructives.grasp.FakeFacilitiesDRFPListManager;
import es.urjc.etsii.grafo.drflp.constructives.tetris.DRFPTetrisConstructive;
import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.drflp.shake.IRSDestructive;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.algorithms.IteratedGreedy;
import es.urjc.etsii.grafo.solver.create.grasp.GreedyRandomGRASPConstructive;
import es.urjc.etsii.grafo.solver.destructor.DestroyRebuild;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.drflp.experiments.CommonConfig.moveBySwapLS;

public class FinalExperiment extends AbstractExperiment<DRFLPSolution, DRFLPInstance> {

    protected FinalExperiment(SolverConfig config) {
        super(config);
    }

    @Override
    public List<Algorithm<DRFLPSolution, DRFLPInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<DRFLPSolution, DRFLPInstance>>();

        algorithms.add(iraceConfig());
        //algorithms.add(myConfig());

        return algorithms;
    }



//    public Algorithm<DRFLPSolution, DRFLPInstance> myConfig(){
//        // el nuevo shake es: new BestPositionShake(), el que supera a sora es el interrow
//        return new FakeIncrementer("RealIG", (fakes) -> new ParallelMultiStartAlgorithm(
//                1000, "RealIG", new IteratedGreedy<>(1000, 100,
//                new DRFPTetrisConstructive(fakes, 0.5d),
//                new DestroyRebuild<>(new DRFPTetrisConstructive(fakes, 0.5d), new IRSDestructive()),
//                moveBySwapLS
//            )),
//            new FIUtil.FibonacciFakesForIteration(),
//            new FIUtil.FractionOfWidthStop(0.1D),
//            10,
//            TimeUnit.MINUTES
//        );
//    }

    public Algorithm<DRFLPSolution, DRFLPInstance> iraceConfig(){
/*
2022-02-06 03:19:08.777  INFO 838498 --- [main] e.u.e.g.s.irace.runners.RLangRunner      :    constructive    reconstructive   stop       increment           iterationsratio alpha1 alpha2 destratio fractionv constantv linearratio
2022-02-06 03:19:08.777  INFO 838498 --- [main] e.u.e.g.s.irace.runners.RLangRunner      :    graspgr         tetris           constant   fiboinc             160             0.0898 0.5518      <NA>        NA        27          NA
*/
        int totalIterations = 1000 * 1000;
        int multiStartIterations = 160; // el ratio
        int iteratedGreedyIterations = totalIterations / multiStartIterations;

        return new FakeIncrementer("MSIG", (fakes) -> new ParallelMultiStartAlgorithm(
                multiStartIterations, "MSIG", new IteratedGreedy<>(iteratedGreedyIterations, 100,
                new GreedyRandomGRASPConstructive<>(new FakeFacilitiesDRFPListManager(fakes), 0.0898d, false),
                new DestroyRebuild<>(new DRFPTetrisConstructive(fakes, 0.5518d), new IRSDestructive()),
                moveBySwapLS
                )),
                new FIUtil.FibonacciFakesForIteration(),
                new FIUtil.ConstantStop(27),
                10,
                TimeUnit.MINUTES);
    }
}
