package es.urjc.etsii.grafo.drflp.reference;

import java.io.IOException;

/**
 * Loads reference results for Heuristic1 as reported in the previous paper
 */
public class H1Reference extends DRFLPReferenceResults {
    public H1Reference() throws IOException {
        super("H1+LP", 1, 2);
    }
}
