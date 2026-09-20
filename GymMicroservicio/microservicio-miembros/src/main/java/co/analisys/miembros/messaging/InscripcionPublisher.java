package co.analisys.miembros.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import co.analisys.miembros.config.RabbitMQConfig;
import co.analisys.miembros.dto.InscripcionEvent;
import co.analisys.miembros.model.Miembro;

@Component
public class InscripcionPublisher {

    private final RabbitTemplate rabbitTemplate;

    public InscripcionPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(Miembro miembro) {
        InscripcionEvent evento = new InscripcionEvent(
                miembro.getId(), miembro.getNombre(), miembro.getEmail(), miembro.getFechaInscripcion());
        rabbitTemplate.convertAndSend(RabbitMQConfig.INSCRIPCIONES_EXCHANGE, "", evento);
    }
}
