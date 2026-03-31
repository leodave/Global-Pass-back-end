package global_pass.users;

// Thrown when login/change-password has wrong password — returns 401
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("Invalid password");
    }
}
