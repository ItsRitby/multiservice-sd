package co.edu.uptc.sd.multiservice.exception.custom.calculator;

public abstract class CalculatorException extends RuntimeException {
    private final String errorCode;

    protected CalculatorException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
