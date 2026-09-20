package co.analisys.clase.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import co.analisys.clase.config.RabbitMQConfig;
import co.analisys.clase.dto.HorarioCambiadoEvent;

@Component
public class HorarioPublisher {

    private final RabbitTemplate rabbitTemplate;

    public HorarioPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(HorarioCambiadoEvent evento) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.HORARIOS_EXCHANGE, "", evento);
    }
}
