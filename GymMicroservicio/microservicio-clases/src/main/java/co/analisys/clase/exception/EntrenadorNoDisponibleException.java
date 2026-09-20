package co.analisys.clase.exception;

public class EntrenadorNoDisponibleException extends RuntimeException {
    public EntrenadorNoDisponibleException(Long entrenadorId) {
        super("El entrenador con id " + entrenadorId + " existe pero no está disponible");
    }
}
