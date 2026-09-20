package co.analisys.miembros.dto;

import java.math.BigDecimal;

public record PagoRequest(Long miembroId, BigDecimal monto) {
}
