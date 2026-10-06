package co.edu.uptc.sd.multiservice.exception.custom.calculator;

public class DivisionByZeroException extends CalculatorException {
    public DivisionByZeroException() {
        super("001", "Division by zero is not allowed.");
    }
}
