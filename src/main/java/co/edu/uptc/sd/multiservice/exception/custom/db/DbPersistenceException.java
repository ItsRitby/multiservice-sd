package co.edu.uptc.sd.multiservice.exception.custom.db;

public class DbPersistenceException extends DatabaseException {
    public DbPersistenceException(String message) {
        super("204", message);
    }
}
