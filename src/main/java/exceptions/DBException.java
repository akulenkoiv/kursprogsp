package exceptions;

public class DBException extends AppException {
    public DBException(String message, Throwable cause) {
        super(message, cause);
    }
}