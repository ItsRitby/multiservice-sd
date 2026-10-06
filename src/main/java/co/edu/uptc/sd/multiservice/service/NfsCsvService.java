package co.edu.uptc.sd.multiservice.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.uptc.sd.multiservice.dto.PersonDTO;
import co.edu.uptc.sd.multiservice.dto.PersonPageResponseDTO;
import co.edu.uptc.sd.multiservice.exception.custom.file.CsvNotFoundException;
import co.edu.uptc.sd.multiservice.exception.custom.file.CsvReadException;
import co.edu.uptc.sd.multiservice.exception.custom.file.PageOutOfRangeException;
import co.edu.uptc.sd.multiservice.exception.custom.file.PersonNotFoundException;
import co.edu.uptc.sd.multiservice.service.index.CsvIndex;
import co.edu.uptc.sd.multiservice.util.Messages;
import co.edu.uptc.sd.multiservice.util.NodeIdentifier;
import jakarta.annotation.PostConstruct;

@Service
public class NfsCsvService {

    private static final Logger log = LoggerFactory.getLogger(NfsCsvService.class);
    private static final long RELOAD_CHECK_TTL_MS = 30_000L;

    private final NodeIdentifier nodeIdentifier;
    private final String csvPath;
    private final int pageSize;
    private final AtomicReference<CsvIndex> indexRef = new AtomicReference<>();
    private volatile long lastReloadCheck = 0L;

    public NfsCsvService(
            NodeIdentifier nodeIdentifier,
            @Value("${app.nfs.csv-path:/data/people.csv}") String csvPath,
            @Value("${app.nfs.page-size:100}") int pageSize) {
        this.nodeIdentifier = nodeIdentifier;
        this.csvPath = csvPath;
        this.pageSize = pageSize;
    }

    @PostConstruct
    void init() {
        try {
            reloadIndex();
        } catch (Exception e) {
            log.error("Failed to build CSV index at startup: {}", e.getMessage());
        }
    }

    public PersonPageResponseDTO getPage(int pageNumber) {
        CsvIndex index = getFreshIndex();
        if (pageNumber < 0 || pageNumber >= index.getTotalPages()) {
            throw new PageOutOfRangeException(pageNumber, index.getTotalPages() - 1);
        }
        long offset = index.getOffsets()[pageNumber];
        List<PersonDTO> persons = readLines(offset, pageSize);
        return new PersonPageResponseDTO(
                nodeIdentifier.getVmHostname(),
                nodeIdentifier.getContainerName(),
                pageNumber,
                pageSize,
                index.getTotalPages(),
                index.getTotalLines(),
                pageNumber + 1 < index.getTotalPages(),
                Messages.FIXED_MESSAGE,
                persons);
    }

    public PersonDTO findById(int id) {
        CsvIndex index = getFreshIndex();
        int left = 0;
        int right = index.getTotalPages() - 1;
        int candidatePage = -1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            int firstId = readFirstId(index.getOffsets()[mid]);
            if (firstId <= id) {
                candidatePage = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        if (candidatePage >= 0) {
            PersonDTO found = findInPage(index.getOffsets()[candidatePage], id);
            if (found != null)
                return found;
        }
        throw new PersonNotFoundException(id);
    }

    private CsvIndex getFreshIndex() {
        long now = System.currentTimeMillis();
        if (now - lastReloadCheck > RELOAD_CHECK_TTL_MS) {
            lastReloadCheck = now;
            try {
                checkForChanges();
            } catch (IOException e) {
                log.warn("Failed to check CSV changes: {}", e.getMessage());
            }
        }
        CsvIndex index = indexRef.get();
        if (index == null) {
            throw new CsvReadException("CSV index is not available.");
        }
        return index;
    }

    private synchronized void checkForChanges() throws IOException {
        Path path = Path.of(csvPath);
        if (!Files.exists(path))
            return;
        long currentMod = Files.getLastModifiedTime(path).toMillis();
        CsvIndex index = indexRef.get();
        if (index == null || currentMod > index.getCsvLastModified()) {
            log.info("CSV change detected, rebuilding index.");
            reloadIndex();
        }
    }

    private void reloadIndex() {
        Path path = Path.of(csvPath);
        if (!Files.exists(path)) {
            throw new CsvNotFoundException(csvPath);
        }
        try {
            CsvIndex newIndex = CsvIndex.build(path, pageSize);
            indexRef.set(newIndex);
            log.info("CSV index built: {} lines, {} pages.",
                    newIndex.getTotalLines(), newIndex.getTotalPages());
        } catch (IOException e) {
            throw new CsvReadException("Failed to build CSV index: " + e.getMessage());
        }
    }

    private List<PersonDTO> readLines(long startOffset, int count) {
        List<PersonDTO> result = new ArrayList<>(count);
        try (FileChannel channel = FileChannel.open(Path.of(csvPath), StandardOpenOption.READ);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Channels.newInputStream(channel), StandardCharsets.UTF_8))) {
            channel.position(startOffset);
            String line;
            while (result.size() < count && (line = reader.readLine()) != null) {
                PersonDTO dto = parseLine(line);
                if (dto != null)
                    result.add(dto);
            }
        } catch (IOException e) {
            throw new CsvReadException("Failed to read CSV page: " + e.getMessage());
        }
        return result;
    }

    private int readFirstId(long offset) {
        try (FileChannel channel = FileChannel.open(Path.of(csvPath), StandardOpenOption.READ);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Channels.newInputStream(channel), StandardCharsets.UTF_8))) {
            channel.position(offset);
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                throw new CsvReadException("Unexpected end of CSV file.");
            }
            int comma = line.indexOf(',');
            if (comma < 0) {
                throw new CsvReadException("Malformed CSV line: " + line);
            }
            return Integer.parseInt(line.substring(0, comma).trim());
        } catch (IOException e) {
            throw new CsvReadException("Failed to read first id: " + e.getMessage());
        }
    }

    private PersonDTO findInPage(long offset, int id) {
        try (FileChannel channel = FileChannel.open(Path.of(csvPath), StandardOpenOption.READ);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Channels.newInputStream(channel), StandardCharsets.UTF_8))) {
            channel.position(offset);
            String line;
            int seen = 0;
            while (seen < pageSize && (line = reader.readLine()) != null) {
                seen++;
                PersonDTO dto = parseLine(line);
                if (dto != null && dto.id() == id)
                    return dto;
            }
        } catch (IOException e) {
            throw new CsvReadException("Failed to search in CSV page: " + e.getMessage());
        }
        return null;
    }

    private PersonDTO parseLine(String line) {
        if (line == null || line.isEmpty())
            return null;
        String[] parts = line.split(",", 5);
        if (parts.length < 5)
            return null;
        try {
            int id = Integer.parseInt(parts[0].trim());
            return new PersonDTO(id, parts[1], parts[2], parts[3], parts[4]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}