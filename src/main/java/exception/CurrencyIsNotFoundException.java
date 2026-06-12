package exception;

public class CurrencyIsNotFoundException extends RuntimeException {
    public CurrencyIsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurrencyIsNotFoundException(String message) {
        super(message);
    }
}
