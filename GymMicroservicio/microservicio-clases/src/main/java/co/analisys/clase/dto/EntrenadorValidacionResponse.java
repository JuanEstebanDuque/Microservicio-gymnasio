package co.analisys.clase.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa la respuesta del microservicio de Entrenador al consultar
 * GET /api/entrenador/{id}.
 *
 * Contrato real expuesto por microservicio-entrenadores:
 *   GET {entrenador.service.url}/api/entrenador/{id}
 *   200 OK       -> { "id": 1, "nombre": "Carlos Rodríguez", "especialidad": "Yoga" }
 *   404 Not Found -> el entrenador no existe (cuerpo vacío)
 *
 * Ese microservicio no expone un endpoint de validación dedicado ni un campo
 * de disponibilidad: la única validación posible desde Clase es la existencia
 * del entrenador (código 200 vs 404).
 */
@Data
@NoArgsConstructor
public class EntrenadorValidacionResponse {
    private Long id;
    private String nombre;
    private String especialidad;
}
