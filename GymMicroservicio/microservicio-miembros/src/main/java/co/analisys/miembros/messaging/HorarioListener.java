package co.analisys.miembros.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import co.analisys.miembros.config.RabbitMQConfig;
import co.analisys.miembros.dto.HorarioCambiadoEvent;

@Component
public class HorarioListener {

    private static final Logger log = LoggerFactory.getLogger(HorarioListener.class);

    @RabbitListener(queues = RabbitMQConfig.HORARIOS_MIEMBROS_QUEUE)
    public void alCambiarHorario(HorarioCambiadoEvent evento) {
        log.info("[MIEMBROS] Aviso a los miembros: la clase '{}' (id {}) cambio de {} a {}",
                evento.nombre(), evento.claseId(), evento.horarioAnterior(), evento.horarioNuevo());
    }
}
