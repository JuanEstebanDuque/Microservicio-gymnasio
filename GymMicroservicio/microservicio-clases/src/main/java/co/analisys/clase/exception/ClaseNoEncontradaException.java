package co.analisys.clase.exception;

public class ClaseNoEncontradaException extends RuntimeException {
    public ClaseNoEncontradaException(Long id) {
        super("No existe una clase con id " + id);
    }
}
