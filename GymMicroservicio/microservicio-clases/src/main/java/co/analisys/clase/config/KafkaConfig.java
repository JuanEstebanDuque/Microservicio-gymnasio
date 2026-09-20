package co.analisys.clase.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_OCUPACION = "ocupacion-clases";
    private static final String RETENCION_7_DIAS_MS = String.valueOf(7L * 24 * 60 * 60 * 1000);

    /**
     * Una sola particion para conservar el orden de todas las actualizaciones, y retencion de
     * 7 dias: el log de Kafka es la fuente para reprocesar/recuperar desde el ultimo checkpoint.
     */
    @Bean
    public NewTopic ocupacionClasesTopic() {
        return TopicBuilder.name(TOPIC_OCUPACION)
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, RETENCION_7_DIAS_MS)
                .build();
    }
}
