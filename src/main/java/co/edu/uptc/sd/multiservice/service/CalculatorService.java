package co.edu.uptc.sd.multiservice.service;

import org.springframework.stereotype.Service;

import co.edu.uptc.sd.multiservice.dto.CalculatorResponseDTO;
import co.edu.uptc.sd.multiservice.exception.custom.calculator.DivisionByZeroException;
import co.edu.uptc.sd.multiservice.exception.custom.calculator.InvalidOperationException;
import co.edu.uptc.sd.multiservice.exception.custom.calculator.NumericOverflowException;
import co.edu.uptc.sd.multiservice.util.Messages;
import co.edu.uptc.sd.multiservice.util.NodeIdentifier;

@Service
public class CalculatorService {
    private final NodeIdentifier nodeIdentifier;

    public CalculatorService(NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    public CalculatorResponseDTO calculate(double num1, double num2, String operation) {
        double result;
        switch (operation.toLowerCase()) {
            case "add":
                result = num1 + num2;
                operation = "addition";
                break;
            case "sub":
                result = num1 - num2;
                operation = "subtraction";
                break;
            case "mult":
                result = num1 * num2;
                operation = "multiplication";
                break;
            case "div":
                result = divide(num1, num2);
                operation = "division";
                break;
            default:
                throw new InvalidOperationException(operation);
        }
        validate(result);
        return new CalculatorResponseDTO(
                nodeIdentifier.getVmHostname(),
                nodeIdentifier.getContainerName(),
                Messages.FIXED_MESSAGE,
                operation,
                num1, num2, result);
    }

    private double divide(double num1, double num2) {
        if (num2 == 0) {
            throw new DivisionByZeroException();
        }
        return num1 / num2;
    }

    private void validate(double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new NumericOverflowException();
        }
    }
}