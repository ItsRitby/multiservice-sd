package co.edu.uptc.sd.multiservice.exception.custom.calculator;

public class InvalidOperationException extends CalculatorException {
    public InvalidOperationException(String operation) {
        super("003", "Invalid operation '" + operation + "'. Allowed: add, sub, mult, div.");
    }
}
