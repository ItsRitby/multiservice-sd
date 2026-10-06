package co.edu.uptc.sd.multiservice.exception.custom.file;

public class PersonNotFoundException extends NfsException {
    public PersonNotFoundException(int id) {
        super("103", "Person with id " + id + " was not found.");
    }
}
