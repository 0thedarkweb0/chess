package dataaccess;

/**
 * Indicates there already is a user with that name in the database
 */
public class AlreadyTakenException extends Exception {
    public AlreadyTakenException(String message) {
        super(message);
    }
}
