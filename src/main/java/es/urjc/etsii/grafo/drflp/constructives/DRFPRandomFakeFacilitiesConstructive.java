package es.urjc.etsii.grafo.drflp.constructives;

import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;

import java.util.Arrays;

/**
 * Generate random solutions with fake facilities for validation purposes
 */
public class DRFPRandomFakeFacilitiesConstructive extends DRFPRandomConstructive {

    private final double[] widths;

    public DRFPRandomFakeFacilitiesConstructive(double[] widths) {
        this.widths = widths;
    }

    @Override
    public String toString() {
        return "RandomFakeConstructor{wID=" + Arrays.hashCode(widths) +  "}";
    }

    @Override
    public DRFLPSolution construct(DRFLPSolution solution) {
        solution.addFakeFacilities(widths);
        return super.construct(solution);
    }
}
