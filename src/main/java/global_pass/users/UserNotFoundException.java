package global_pass.users;

// Thrown when user is not found by id or email — returns 404
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
