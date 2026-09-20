package co.analisys.miembros.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.analisys.miembros.config.RabbitMQConfig;
import co.analisys.miembros.dto.PagoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pago")
@Tag(name = "Pagos", description = "Registro asincrono de pagos de membresia (RabbitMQ)")
public class PagoController {

    private final RabbitTemplate rabbitTemplate;

    public PagoController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Operation(summary = "Enviar un pago a procesar", description = "Publica el pago en pagos-queue; se procesa de forma asincrona y si falla termina en pagos-dlq. Roles permitidos: ADMIN, MEMBER.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Pago encolado para procesamiento"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El rol del usuario no tiene permiso", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void enviarPago(@RequestBody PagoRequest pago) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAGOS_QUEUE, pago);
    }
}
