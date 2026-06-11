package exception;

public class MissingRequireFieldException extends RuntimeException {
    public MissingRequireFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingRequireFieldException(String message) {
        super(message);
    }
}
