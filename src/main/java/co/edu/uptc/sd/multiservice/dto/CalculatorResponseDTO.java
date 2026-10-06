package co.edu.uptc.sd.multiservice.dto;

public record CalculatorResponseDTO(
        String vmName,
        String containerName,
        String operation,
        double num1,
        double num2,
        double result,
        String message) {
}