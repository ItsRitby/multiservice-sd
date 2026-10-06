package co.edu.uptc.sd.multiservice.dto;

import java.util.List;

public record PersonPageResponseDTO(
        String vmHostname,
        String containerName,
        int pageNumber,
        int pageSize,
        int totalPages,
        long totalRecords,
        boolean hasNext,
        String message,
        List<PersonDTO> persons) {
}