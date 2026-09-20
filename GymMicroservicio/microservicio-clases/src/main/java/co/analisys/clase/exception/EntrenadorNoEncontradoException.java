package co.analisys.clase.exception;

public class EntrenadorNoEncontradoException extends RuntimeException {
    public EntrenadorNoEncontradoException(Long entrenadorId) {
        super("No existe un entrenador con id " + entrenadorId);
    }
}
