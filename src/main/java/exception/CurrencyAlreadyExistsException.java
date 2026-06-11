package exception;

public class CurrencyAlreadyExistsException extends RuntimeException {
    public CurrencyAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurrencyAlreadyExistsException(String message) {
        super(message);
    }
}
