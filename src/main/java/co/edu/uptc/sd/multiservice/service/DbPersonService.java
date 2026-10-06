package co.edu.uptc.sd.multiservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uptc.sd.multiservice.dto.PersonDTO;
import co.edu.uptc.sd.multiservice.dto.PersonPageResponseDTO;
import co.edu.uptc.sd.multiservice.dto.PersonResponseDTO;
import co.edu.uptc.sd.multiservice.dto.PersonUpdateRequestDTO;
import co.edu.uptc.sd.multiservice.entity.PersonEntity;
import co.edu.uptc.sd.multiservice.exception.custom.db.DbPageOutOfRangeException;
import co.edu.uptc.sd.multiservice.exception.custom.db.DbPersonNotFoundException;
import co.edu.uptc.sd.multiservice.exception.custom.db.InvalidPersonDataException;
import co.edu.uptc.sd.multiservice.repository.PersonRepository;
import co.edu.uptc.sd.multiservice.util.Messages;
import co.edu.uptc.sd.multiservice.util.NodeIdentifier;

@Service
public class DbPersonService {

    private static final Logger log = LoggerFactory.getLogger(DbPersonService.class);

    private final PersonRepository personRepository;
    private final NodeIdentifier nodeIdentifier;
    private final int pageSize;

    private volatile long cachedTotalElements = -1;

    public DbPersonService(PersonRepository personRepository,
            NodeIdentifier nodeIdentifier,
            @Value("${app.db.page-size:100}") int pageSize) {
        this.personRepository = personRepository;
        this.nodeIdentifier = nodeIdentifier;
        this.pageSize = pageSize;
    }

    public PersonPageResponseDTO getPage(int pageNumber) {
        long total = getTotalElements();
        int totalPages = (int) Math.ceil((double) total / pageSize);

        if (pageNumber < 0 || pageNumber >= totalPages) {
            throw new DbPageOutOfRangeException(pageNumber, totalPages - 1);
        }

        int from = pageNumber * pageSize + 1;
        int to = from + pageSize - 1;

        List<PersonDTO> persons = personRepository.findPageByIdRange(from, to);
        boolean hasNext = pageNumber + 1 < totalPages;

        return new PersonPageResponseDTO(
                nodeIdentifier.getVmHostname(),
                nodeIdentifier.getContainerName(),
                pageNumber,
                pageSize,
                totalPages,
                total,
                hasNext,
                Messages.FIXED_MESSAGE,
                persons);
    }

    public PersonResponseDTO findById(int id) {
        PersonEntity entity = personRepository.findById(id)
                .orElseThrow(() -> new DbPersonNotFoundException(id));
        return toResponse(entity);
    }

    @Transactional
    public PersonResponseDTO update(PersonUpdateRequestDTO request) {
        validate(request);
        PersonEntity entity = personRepository.findById(request.id())
                .orElseThrow(() -> new DbPersonNotFoundException(request.id()));

        entity.setFirstName(request.firstName());
        entity.setMiddleName(request.middleName());
        entity.setLastName1(request.lastName1());
        entity.setLastName2(request.lastName2());

        return toResponse(personRepository.save(entity));
    }

    private long getTotalElements() {
        long cached = cachedTotalElements;
        if (cached < 0) {
            synchronized (this) {
                cached = cachedTotalElements;
                if (cached < 0) {
                    cached = personRepository.findMaxId().orElse(0).longValue();
                    cachedTotalElements = cached;
                    log.info("Cached person max id: {}", cached);
                }
            }
        }
        return cached;
    }

    private void validate(PersonUpdateRequestDTO request) {
        if (request.id() == null) {
            throw new InvalidPersonDataException("id");
        }
        if (isBlank(request.firstName())) {
            throw new InvalidPersonDataException("firstName");
        }
        if (isBlank(request.lastName1())) {
            throw new InvalidPersonDataException("lastName1");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private PersonResponseDTO toResponse(PersonEntity entity) {
        return new PersonResponseDTO(
                nodeIdentifier.getVmHostname(),
                nodeIdentifier.getContainerName(),
                entity.getId(),
                entity.getFirstName(),
                entity.getMiddleName(),
                entity.getLastName1(),
                entity.getLastName2());
    }
}