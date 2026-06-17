package exception;

public class CurrencyIsNotFoundException extends RuntimeException {
    public CurrencyIsNotFoundException(String message) {
        super(message);
    }
}
