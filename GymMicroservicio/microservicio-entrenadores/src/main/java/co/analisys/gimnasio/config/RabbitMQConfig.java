package co.analisys.gimnasio.config;

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

    public static final String HORARIOS_EXCHANGE = "horarios-exchange";
    public static final String HORARIOS_ENTRENADORES_QUEUE = "horarios-entrenadores-queue";

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // Publish/subscribe de horarios: clases publica, entrenadores se suscribe con su propia cola
    @Bean
    public FanoutExchange horariosExchange() {
        return new FanoutExchange(HORARIOS_EXCHANGE);
    }

    @Bean
    public Queue horariosEntrenadoresQueue() {
        return QueueBuilder.durable(HORARIOS_ENTRENADORES_QUEUE).build();
    }

    @Bean
    public Binding horariosEntrenadoresBinding() {
        return BindingBuilder.bind(horariosEntrenadoresQueue()).to(horariosExchange());
    }
}
