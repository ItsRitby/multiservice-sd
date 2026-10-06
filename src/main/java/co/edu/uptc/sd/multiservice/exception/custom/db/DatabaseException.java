package co.edu.uptc.sd.multiservice.exception.custom.db;

public abstract class DatabaseException extends RuntimeException {

    private final String errorCode;

    protected DatabaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
