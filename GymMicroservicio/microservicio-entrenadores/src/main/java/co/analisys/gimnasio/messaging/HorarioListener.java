package co.analisys.gimnasio.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import co.analisys.gimnasio.config.RabbitMQConfig;
import co.analisys.gimnasio.dto.HorarioCambiadoEvent;

@Component
public class HorarioListener {

    private static final Logger log = LoggerFactory.getLogger(HorarioListener.class);

    @RabbitListener(queues = RabbitMQConfig.HORARIOS_ENTRENADORES_QUEUE)
    public void alCambiarHorario(HorarioCambiadoEvent evento) {
        log.info("[ENTRENADORES] Aviso a los entrenadores: la clase '{}' (id {}) cambio de {} a {}",
                evento.nombre(), evento.claseId(), evento.horarioAnterior(), evento.horarioNuevo());
    }
}
