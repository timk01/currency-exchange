package exception;

public class ExchangeRatePairDoesNotExistException extends RuntimeException {
    public ExchangeRatePairDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExchangeRatePairDoesNotExistException(String message) {
        super(message);
    }
}
