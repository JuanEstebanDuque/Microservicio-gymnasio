package co.analisys.clase.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import co.analisys.clase.dto.EntrenadorValidacionResponse;
import co.analisys.clase.exception.EntrenadorNoEncontradoException;
import co.analisys.clase.exception.EntrenadorServiceUnavailableException;

/**
 * Comunicación REST con el microservicio de Entrenador (Bounded Context externo).
 *
 * microservicio-entrenadores no expone un endpoint de validación dedicado; expone
 * un CRUD estándar. Clase reutiliza su endpoint de consulta por id para validar
 * que el entrenador referenciado exista antes de programar una clase:
 *
 *   GET /api/entrenador/{id}  -> 200 (existe) | 404 (no existe)
 */
@Component
public class EntrenadorClient {

    private static final String PATH_ENTRENADOR_POR_ID = "/api/entrenador/{id}";

    private final RestClient restClient;

    public EntrenadorClient(RestClient entrenadorRestClient) {
        this.restClient = entrenadorRestClient;
    }

    /**
     * Consulta al microservicio de Entrenador si el entrenador existe.
     * Lanza EntrenadorNoEncontradoException si no existe (404),
     * y EntrenadorServiceUnavailableException si el servicio no responde.
     */
    public EntrenadorValidacionResponse validarEntrenador(Long entrenadorId) {
        try {
            return restClient.get()
                    .uri(PATH_ENTRENADOR_POR_ID, entrenadorId)
                    .retrieve()
                    .body(EntrenadorValidacionResponse.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new EntrenadorNoEncontradoException(entrenadorId);
        } catch (ResourceAccessException ex) {
            throw new EntrenadorServiceUnavailableException(
                    "No fue posible contactar al microservicio de Entrenador", ex);
        }
    }
}
