package co.edu.uptc.sd.multiservice.exception.custom.db;

public class DbPageOutOfRangeException extends DatabaseException {
    public DbPageOutOfRangeException(int page, int lastPage) {
        super("203", "Page " + page + " is out of range [0, " + lastPage + "].");
    }
}
