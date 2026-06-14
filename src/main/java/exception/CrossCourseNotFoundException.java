package exception;

public class CrossCourseNotFoundException extends RuntimeException {
    public CrossCourseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrossCourseNotFoundException(String message) {
        super(message);
    }
}

