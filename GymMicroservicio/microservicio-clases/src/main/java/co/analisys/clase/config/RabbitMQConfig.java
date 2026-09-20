package co.analisys.clase.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String INSCRIPCIONES_EXCHANGE = "inscripciones-exchange";
    public static final String INSCRIPCIONES_CLASES_QUEUE = "inscripciones-clases-queue";
    public static final String HORARIOS_EXCHANGE = "horarios-exchange";

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // Notificaciones de inscripcion: miembros publica, clases consume
    @Bean
    public FanoutExchange inscripcionesExchange() {
        return new FanoutExchange(INSCRIPCIONES_EXCHANGE);
    }

    @Bean
    public Queue inscripcionesClasesQueue() {
        return QueueBuilder.durable(INSCRIPCIONES_CLASES_QUEUE).build();
    }

    @Bean
    public Binding inscripcionesClasesBinding() {
        return BindingBuilder.bind(inscripcionesClasesQueue()).to(inscripcionesExchange());
    }

    // Publish/subscribe de horarios: clases publica, miembros y entrenadores se suscriben
    @Bean
    public FanoutExchange horariosExchange() {
        return new FanoutExchange(HORARIOS_EXCHANGE);
    }
}
