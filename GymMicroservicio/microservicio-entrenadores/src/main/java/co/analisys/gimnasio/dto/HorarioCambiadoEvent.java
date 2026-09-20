package co.analisys.gimnasio.dto;

import java.time.LocalDateTime;

public record HorarioCambiadoEvent(Long claseId, String nombre, LocalDateTime horarioAnterior, LocalDateTime horarioNuevo) {
}
