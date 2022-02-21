package es.urjc.etsii.grafo.drflp.reference;

import es.urjc.etsii.grafo.solver.services.reference.ReferenceResult;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import org.apache.commons.collections4.map.HashedMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Loads reference results as reported in the previous paper
 */
public abstract class DRFLPReferenceResults extends ReferenceResultProvider {
    private final String name;
    Map<String, ReferenceResult> sotaResults = new HashedMap<>();

    public DRFLPReferenceResults(String name, int scoreIndex, int timeIndex) throws IOException {
        this.name = name;
        Files.lines(Path.of("sota.tsv")).forEach(l -> {
            var parts = l.split("\t");
            var referenceResult = new ReferenceResult();
            referenceResult.setScore(parts[scoreIndex]);
            referenceResult.setTimeInSeconds(parts[timeIndex]);
            sotaResults.put(parts[0], referenceResult);
        });
    }

    @Override
    public ReferenceResult getValueFor(String instanceName) {
        return this.sotaResults.getOrDefault(instanceName, new ReferenceResult());
    }

    @Override
    public String getProviderName() {
        return name;
    }
}
