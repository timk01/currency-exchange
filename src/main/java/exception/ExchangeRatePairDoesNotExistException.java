package exception;

public class ExchangeRatePairDoesNotExistException extends RuntimeException {
    public ExchangeRatePairDoesNotExistException(String message) {
        super(message);
    }
}
