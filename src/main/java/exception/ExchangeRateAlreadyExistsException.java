package exception;

public class ExchangeRateAlreadyExistsException extends RuntimeException {
    public ExchangeRateAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExchangeRateAlreadyExistsException(String message) {
        super(message);
    }
}
