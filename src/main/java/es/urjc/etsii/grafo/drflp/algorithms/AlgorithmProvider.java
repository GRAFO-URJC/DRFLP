package es.urjc.etsii.grafo.drflp.algorithms;

import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

/**
 * Functional interface to build an algorithm given a set of fake facilities
 */
@FunctionalInterface
public interface AlgorithmProvider {
    Algorithm<DRFLPSolution, DRFLPInstance> build(double[] fakes);
}
