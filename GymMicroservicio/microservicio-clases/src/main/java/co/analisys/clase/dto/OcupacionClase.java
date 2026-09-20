package co.analisys.clase.dto;

import java.time.LocalDateTime;

public record OcupacionClase(Long claseId, int ocupacionActual, LocalDateTime timestamp) {
}
