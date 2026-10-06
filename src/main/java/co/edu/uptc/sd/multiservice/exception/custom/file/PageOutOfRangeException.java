package co.edu.uptc.sd.multiservice.exception.custom.file;

public class PageOutOfRangeException extends NfsException {
    public PageOutOfRangeException(int page, int lastPage) {
        super("102", "Page " + page + " is out of range [0, " + lastPage + "].");
    }
}
