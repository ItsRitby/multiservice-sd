package co.edu.uptc.sd.multiservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uptc.sd.multiservice.dto.PersonDTO;
import co.edu.uptc.sd.multiservice.dto.PersonPageResponseDTO;
import co.edu.uptc.sd.multiservice.service.NfsCsvService;


@RestController
@RequestMapping("/api/nfs")
public class NfsController {

    private final NfsCsvService nfsCsvService;

    public NfsController(NfsCsvService nfsCsvService) {
        this.nfsCsvService = nfsCsvService;
    }

    @GetMapping
    public PersonPageResponseDTO getPage(
            @RequestParam(value = "page", defaultValue = "0") int page) {
        return nfsCsvService.getPage(page);
    }

    @GetMapping("/{id}")
    public PersonDTO findById(@PathVariable int id) {
        return nfsCsvService.findById(id);
    }
}
