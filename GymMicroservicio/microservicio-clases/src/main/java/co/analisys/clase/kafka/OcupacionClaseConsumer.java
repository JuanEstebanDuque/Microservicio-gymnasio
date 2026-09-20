package co.analisys.clase.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import co.analisys.clase.config.KafkaConfig;
import co.analisys.clase.dto.OcupacionClase;

@Service
public class OcupacionClaseConsumer {

    private static final Logger log = LoggerFactory.getLogger(OcupacionClaseConsumer.class);

    private final DashboardOcupacion dashboard;

    public OcupacionClaseConsumer(DashboardOcupacion dashboard) {
        this.dashboard = dashboard;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_OCUPACION, groupId = "monitoreo-grupo")
    public void consumirActualizacionOcupacion(OcupacionClase ocupacion) {
        log.info("[KAFKA-MONITOREO] Clase {} ocupacion {} ({})",
                ocupacion.claseId(), ocupacion.ocupacionActual(), ocupacion.timestamp());
        dashboard.actualizar(ocupacion);
    }
}
