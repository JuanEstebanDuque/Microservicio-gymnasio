package co.analisys.clase.exception;

public class EntrenadorServiceUnavailableException extends RuntimeException {
    public EntrenadorServiceUnavailableException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
