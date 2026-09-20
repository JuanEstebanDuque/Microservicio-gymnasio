package co.analisys.clase.kafka;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.analisys.clase.dto.OcupacionClase;
import co.analisys.clase.model.CheckpointOffset;
import co.analisys.clase.model.OcupacionHistorial;
import co.analisys.clase.repository.CheckpointOffsetRepository;
import co.analisys.clase.repository.OcupacionHistorialRepository;

/**
 * Guarda el resultado del procesamiento y el checkpoint (offset) en una unica transaccion:
 * si el proceso cae, nunca hay un offset avanzado sin su resultado ni un resultado sin offset.
 */
@Service
public class CheckpointService {

    private final OcupacionHistorialRepository historialRepository;
    private final CheckpointOffsetRepository checkpointRepository;

    public CheckpointService(OcupacionHistorialRepository historialRepository,
            CheckpointOffsetRepository checkpointRepository) {
        this.historialRepository = historialRepository;
        this.checkpointRepository = checkpointRepository;
    }

    @Transactional
    public void procesarYGuardar(String topic, int particion, long offset, OcupacionClase ocupacion) {
        OcupacionHistorial h = new OcupacionHistorial();
        h.setClaseId(ocupacion.claseId());
        h.setOcupacionActual(ocupacion.ocupacionActual());
        h.setMomento(ocupacion.timestamp() != null ? ocupacion.timestamp() : LocalDateTime.now());
        h.setKafkaPartition(particion);
        h.setKafkaOffset(offset);
        historialRepository.save(h);

        CheckpointOffset c = new CheckpointOffset();
        c.setId(topic + "-" + particion);
        c.setTopic(topic);
        c.setKafkaPartition(particion);
        c.setUltimoOffset(offset);
        checkpointRepository.save(c);
    }

    public Long cargarUltimoOffset(String topic, int particion) {
        return checkpointRepository.findById(topic + "-" + particion)
                .map(CheckpointOffset::getUltimoOffset).orElse(null);
    }
}
