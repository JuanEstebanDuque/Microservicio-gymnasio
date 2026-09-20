package co.analisys.clase.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntrenadorNoEncontradoException.class)
    public ResponseEntity<Object> handleEntrenadorNoEncontrado(EntrenadorNoEncontradoException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EntrenadorServiceUnavailableException.class)
    public ResponseEntity<Object> handleEntrenadorServiceUnavailable(EntrenadorServiceUnavailableException ex) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible validar el entrenador: el microservicio de Entrenador no está disponible");
    }

    @ExceptionHandler(ClaseNoEncontradaException.class)
    public ResponseEntity<Object> handleClaseNoEncontrada(ClaseNoEncontradaException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<Object> respuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
