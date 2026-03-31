package global_pass.users;

// Thrown when signup/update uses an email that already exists — returns 409
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }
}
