package co.analisys.clase.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import co.analisys.clase.config.RabbitMQConfig;
import co.analisys.clase.dto.InscripcionEvent;

@Component
public class InscripcionListener {

    private static final Logger log = LoggerFactory.getLogger(InscripcionListener.class);

    @RabbitListener(queues = RabbitMQConfig.INSCRIPCIONES_CLASES_QUEUE)
    public void alInscribirseMiembro(InscripcionEvent evento) {
        log.info("[CLASES] Notificacion: nuevo miembro inscrito -> {} ({}), id {}, desde {}",
                evento.nombre(), evento.email(), evento.miembroId(), evento.fechaInscripcion());
    }
}
