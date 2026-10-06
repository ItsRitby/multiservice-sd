package co.edu.uptc.sd.multiservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uptc.sd.multiservice.dto.PersonPageResponseDTO;
import co.edu.uptc.sd.multiservice.dto.PersonResponseDTO;
import co.edu.uptc.sd.multiservice.dto.PersonUpdateRequestDTO;
import co.edu.uptc.sd.multiservice.service.DbPersonService;

@RestController
@RequestMapping("/api/db")
public class DbController {

    private final DbPersonService dbPersonService;

    public DbController(DbPersonService dbPersonService) {
        this.dbPersonService = dbPersonService;
    }

    @GetMapping
    public PersonPageResponseDTO getPage(
            @RequestParam(value = "page", defaultValue = "0") int page) {
        return dbPersonService.getPage(page);
    }

    @GetMapping("/{id}")
    public PersonResponseDTO findById(@PathVariable int id) {
        return dbPersonService.findById(id);
    }

    @PostMapping
    public PersonResponseDTO update(@RequestBody PersonUpdateRequestDTO request) {
        return dbPersonService.update(request);
    }
}
