package co.analisys.miembros.dto;

import java.time.LocalDateTime;

public record DatosEntrenamiento(Long miembroId, String ejercicio, int duracionMin, int calorias, LocalDateTime fecha) {
}
