package co.edu.uptc.sd.multiservice.dto;

public record PersonResponseDTO(
        String vmHostname,
        String containerName,
        int id,
        String firstName,
        String middleName,
        String lastName1,
        String lastName2) {
}