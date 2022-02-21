package es.urjc.etsii.grafo.drflp.io;

import es.urjc.etsii.grafo.io.serializers.AbstractSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Custom configuration for DRFPSolutionIO, so it can be configured in the application.yml file
 */
@Configuration
@ConfigurationProperties(prefix = "serializers.solution-drfp")
public class DRFPSolutionSerializerConfig extends AbstractSerializerConfig {
}
