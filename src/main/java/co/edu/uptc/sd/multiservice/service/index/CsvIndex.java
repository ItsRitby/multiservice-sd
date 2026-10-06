package co.edu.uptc.sd.multiservice.service.index;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvIndex {

    private final long[] offsets;
    private final long totalLines;
    private final long csvLastModified;

    public CsvIndex(long[] offsets, long totalLines, long csvLastModified) {
        this.offsets = offsets;
        this.totalLines = totalLines;
        this.csvLastModified = csvLastModified;
    }

    public long[] getOffsets() {
        return offsets;
    }

    public long getTotalLines() {
        return totalLines;
    }

    public long getCsvLastModified() {
        return csvLastModified;
    }

    public int getTotalPages() {
        return offsets.length;
    }

    /** Scans the CSV once at byte level and records the start offset of each page. */
    public static CsvIndex build(Path csvPath, int pageSize) throws IOException {
        long fileSize = Files.size(csvPath);
        long lastModified = Files.getLastModifiedTime(csvPath).toMillis();

        List<Long> collectedOffsets = new ArrayList<>();
        collectedOffsets.add(0L);

        long lineCount = 0;
        long bytePos = 0;
        int lastByte = -1;

        try (InputStream raw = Files.newInputStream(csvPath);
             BufferedInputStream in = new BufferedInputStream(raw, 64 * 1024)) {
            int b;
            while ((b = in.read()) != -1) {
                bytePos++;
                lastByte = b;
                if (b == '\n') {
                    lineCount++;
                    if (lineCount % pageSize == 0 && bytePos < fileSize) {
                        collectedOffsets.add(bytePos);
                    }
                }
            }
        }

        if (bytePos > 0 && lastByte != '\n') {
            lineCount++;
        }

        long[] offsets = new long[collectedOffsets.size()];
        for (int i = 0; i < offsets.length; i++) {
            offsets[i] = collectedOffsets.get(i);
        }
        return new CsvIndex(offsets, lineCount, lastModified);
    }
}
