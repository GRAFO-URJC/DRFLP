package es.urjc.etsii.grafo.drflp.constructives;

import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.drflp.model.Facility;
import es.urjc.etsii.grafo.solver.create.Reconstructive;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;

/**
 * Generate random solutions for validation purposes
 */
public class DRFPRandomConstructive extends Reconstructive<DRFLPSolution, DRFLPInstance> {

    @Override
    public DRFLPSolution construct(DRFLPSolution solution) {
        var facilities = new ArrayList<>(solution.getNotAssignedFacilities());
        if(facilities.isEmpty()){
            return solution;
        }

        var random = RandomManager.getRandom();
        CollectionUtil.shuffle(facilities);

        var nrows = solution.getInstance().getNRows();

        for (Facility f : facilities) {
            int row = random.nextInt(nrows);
            solution.insertLast(row, f);
        }

        solution.rebuildCaches();
        solution.updateLastModifiedTime();

        return solution;
    }

    @Override
    public DRFLPSolution reconstruct(DRFLPSolution solution) {
        return construct(solution);
    }
}
