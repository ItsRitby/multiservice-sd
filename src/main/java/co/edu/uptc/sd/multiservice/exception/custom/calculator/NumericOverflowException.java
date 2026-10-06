package co.edu.uptc.sd.multiservice.exception.custom.calculator;

public class NumericOverflowException extends CalculatorException {
    public NumericOverflowException() {
        super("002", "The operation result exceeds the numeric limit.");
    }
}
