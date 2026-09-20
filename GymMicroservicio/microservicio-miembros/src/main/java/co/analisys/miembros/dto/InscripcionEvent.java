package co.analisys.miembros.dto;

import java.time.LocalDate;

public record InscripcionEvent(Long miembroId, String nombre, String email, LocalDate fechaInscripcion) {
}
