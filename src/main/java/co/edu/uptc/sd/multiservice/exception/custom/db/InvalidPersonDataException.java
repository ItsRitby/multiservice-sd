package co.edu.uptc.sd.multiservice.exception.custom.db;

public class InvalidPersonDataException extends DatabaseException {
    public InvalidPersonDataException(String field) {
        super("202", "Invalid or missing value for field '" + field + "'.");
    }
}
