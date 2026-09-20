package co.analisys.miembros.messaging;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import co.analisys.miembros.config.RabbitMQConfig;
import co.analisys.miembros.dto.PagoRequest;

/**
 * Consume pagos-queue. Si el pago falla, se reintenta (spring.rabbitmq.listener.simple.retry)
 * y al agotar los intentos el mensaje se rechaza sin reencolar: RabbitMQ lo enruta a pagos-dlq.
 */
@Component
public class PagoProcessor {

    private static final Logger log = LoggerFactory.getLogger(PagoProcessor.class);
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("5000000");

    @RabbitListener(queues = RabbitMQConfig.PAGOS_QUEUE)
    public void procesarPago(PagoRequest pago) {
        try {
            if (!procesoPagoExitoso(pago)) {
                throw new IllegalStateException("Fallo en el procesamiento del pago");
            }
            log.info("[PAGOS] Pago aprobado: miembro {} monto {}", pago.miembroId(), pago.monto());
        } catch (Exception e) {
            log.warn("[PAGOS] Pago fallido (miembro {}, monto {}): {}", pago.miembroId(), pago.monto(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Error en el pago, enviando a DLQ", e);
        }
    }

    private boolean procesoPagoExitoso(PagoRequest pago) {
        return pago.miembroId() != null
                && pago.monto() != null
                && pago.monto().signum() > 0
                && pago.monto().compareTo(MONTO_MAXIMO) <= 0;
    }
}
