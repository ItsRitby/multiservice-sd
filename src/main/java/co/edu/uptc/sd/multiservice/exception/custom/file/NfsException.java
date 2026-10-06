package co.edu.uptc.sd.multiservice.exception.custom.file;

public abstract class NfsException extends RuntimeException {

    private final String errorCode;

    protected NfsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
