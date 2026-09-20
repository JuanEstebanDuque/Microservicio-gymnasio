package co.analisys.miembros.config;

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
    public static final String HORARIOS_EXCHANGE = "horarios-exchange";
    public static final String HORARIOS_MIEMBROS_QUEUE = "horarios-miembros-queue";
    public static final String PAGOS_QUEUE = "pagos-queue";
    public static final String PAGOS_DLQ = "pagos-dlq";

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // --- Notificaciones de inscripcion (miembros publica, clases consume) ---
    @Bean
    public FanoutExchange inscripcionesExchange() {
        return new FanoutExchange(INSCRIPCIONES_EXCHANGE);
    }

    // --- Publish/subscribe de horarios (clases publica, miembros escucha) ---
    @Bean
    public FanoutExchange horariosExchange() {
        return new FanoutExchange(HORARIOS_EXCHANGE);
    }

    @Bean
    public Queue horariosMiembrosQueue() {
        return QueueBuilder.durable(HORARIOS_MIEMBROS_QUEUE).build();
    }

    @Bean
    public Binding horariosMiembrosBinding() {
        return BindingBuilder.bind(horariosMiembrosQueue()).to(horariosExchange());
    }

    // --- Pagos con Dead Letter Queue ---
    @Bean
    public Queue pagosQueue() {
        return QueueBuilder.durable(PAGOS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", PAGOS_DLQ)
                .withArgument("x-message-ttl", 30000) // 30 segundos
                .build();
    }

    @Bean
    public Queue pagosDlq() {
        return QueueBuilder.durable(PAGOS_DLQ).build();
    }
}
