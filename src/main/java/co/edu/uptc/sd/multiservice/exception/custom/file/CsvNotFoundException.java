package co.edu.uptc.sd.multiservice.exception.custom.file;

public class CsvNotFoundException extends NfsException {
    public CsvNotFoundException(String path) {
        super("100", "CSV file not found at path: " + path);
    }
}
