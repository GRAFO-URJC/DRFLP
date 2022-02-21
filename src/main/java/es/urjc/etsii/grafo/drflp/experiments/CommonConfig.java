package es.urjc.etsii.grafo.drflp.experiments;

import es.urjc.etsii.grafo.drflp.model.*;
import es.urjc.etsii.grafo.solver.algorithms.VNS;
import es.urjc.etsii.grafo.solver.improve.ls.LocalSearchBestImprovement;

public class CommonConfig {
    public static final VNS.KProvider<DRFLPInstance> kProvider = (instance, kIndex) -> {
        int kValue = kIndex;
        if(kValue > instance.getNRealFacilities() * 0.1){
            return VNS.KProvider.STOPNOW;
        } else {
            return kValue;
        }
    };

    public static final int ITER = 1000;

    public static final LocalSearchBestImprovement<DRFLPBaseMove, DRFLPSolution, DRFLPInstance> exchangeLS = new LocalSearchBestImprovement<>(false, new SwapNeighborhood());
    public static final LocalSearchBestImprovement<DRFLPBaseMove, DRFLPSolution, DRFLPInstance> moveLS = new LocalSearchBestImprovement<>(false, new MoveNeighborhood());
    public static final LocalSearchBestImprovement<DRFLPBaseMove, DRFLPSolution, DRFLPInstance> optLS = new LocalSearchBestImprovement<>(false, new OptNeighborhood());
    public static final LocalSearchBestImprovement<MoveBySwapNeighborhood.MoveBySwap, DRFLPSolution, DRFLPInstance> moveBySwapLS = new LocalSearchBestImprovement<>(false, new MoveBySwapNeighborhood());
}
