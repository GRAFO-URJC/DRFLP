package es.urjc.etsii.grafo.drflp.io;

import es.urjc.etsii.grafo.drflp.model.DRFLPInstance;
import es.urjc.etsii.grafo.drflp.model.DRFLPSolution;
import es.urjc.etsii.grafo.io.serializers.SolutionSerializer;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Serialize solutions using a custom format
 * Each row represents a facility row, with positive numbers being the facility id, and negative numbers a fake facility and its width.
 * Example: 1 3 5 -2 --> Facility 1, 3 and 5, and a fake facility with width 2.
 */
public class DRFPSolutionIO extends SolutionSerializer<DRFLPSolution, DRFLPInstance> {

    /**
     * Create a new solution serializer with the given config
     *
     * @param config
     */
    public DRFPSolutionIO(DRFPSolutionSerializerConfig config) {
        super(config);
    }

    @Override
    public void export(BufferedWriter writer, DRFLPSolution solution) throws IOException {
        var data = solution.getSolutionData();
        StringBuilder sb = new StringBuilder();
        for(var row: data){
            for(var f: row){
                if(f == null){
                    continue;
                }
                if(f.facility.fake){
                    sb.append(-f.facility.width);
                } else {
                    sb.append(f.facility.id);
                }
                sb.append(" ");
            }
            if(sb.length() > 0){
                sb.setCharAt(sb.length() - 1, '\n');
            } else {
                sb.append('\n');
            }
        }
        writer.write(sb.toString());
    }
}
