package co.edu.uptc.sd.multiservice.dto;

public record PersonUpdateRequestDTO(
        Integer id,
        String firstName,
        String middleName,
        String lastName1,
        String lastName2) {
}