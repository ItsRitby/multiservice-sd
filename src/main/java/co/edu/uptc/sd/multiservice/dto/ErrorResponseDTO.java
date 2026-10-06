package co.edu.uptc.sd.multiservice.dto;

public record ErrorResponseDTO(
        String errorCode,
        String message,
        String vmName,
        String containerName) {
}