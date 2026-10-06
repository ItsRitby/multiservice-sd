package co.edu.uptc.sd.multiservice.exception.custom.file;

public class CsvReadException extends NfsException {
    public CsvReadException(String message) {
        super("101", message);
    }
}
