
package co.edu.uptc.sd.multiservice.exception.custom.db;

public class DbPersonNotFoundException extends DatabaseException {
    public DbPersonNotFoundException(int id) {
        super("201", "Person with id " + id + " was not found.");
    }
}
