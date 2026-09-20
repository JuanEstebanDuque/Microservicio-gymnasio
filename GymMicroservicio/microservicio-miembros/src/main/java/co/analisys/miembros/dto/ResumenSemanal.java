package co.analisys.miembros.dto;

import java.time.Instant;

public record ResumenSemanal(Instant inicioVentana, Instant finVentana, ResumenEntrenamiento resumen) {
}
