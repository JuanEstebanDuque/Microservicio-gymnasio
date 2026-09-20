package co.analisys.clase.kafka;

import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import co.analisys.clase.config.KafkaConfig;
import co.analisys.clase.dto.OcupacionClase;

@Service
public class OcupacionClaseProducer {

    private final KafkaTemplate<String, OcupacionClase> kafkaTemplate;

    public OcupacionClaseProducer(KafkaTemplate<String, OcupacionClase> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void actualizarOcupacion(Long claseId, int ocupacionActual) {
        OcupacionClase ocupacion = new OcupacionClase(claseId, ocupacionActual, LocalDateTime.now());
        // La clave es el id de la clase: todas sus actualizaciones quedan en orden
        kafkaTemplate.send(KafkaConfig.TOPIC_OCUPACION, String.valueOf(claseId), ocupacion);
    }
}
