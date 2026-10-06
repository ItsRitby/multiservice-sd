package co.edu.uptc.sd.multiservice.dto;

public record CalculatorResponseDTO(
        String vmName,
        String containerName,
        String message,
        String operation,
        double num1,
        double num2,
        double result
        ) {
}