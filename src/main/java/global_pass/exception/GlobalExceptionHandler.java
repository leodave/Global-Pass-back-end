package global_pass.exception;

import global_pass.users.UserApiResponseDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles validation errors (@NotBlank, @Email, @Size etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UserApiResponseDto<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Validation error: {}", message);
        return ResponseEntity.badRequest().body(UserApiResponseDto.<Void>builder()
                .status(400)
                .message(message)
                .build());
    }

    // Handles user not found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<UserApiResponseDto<Void>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UserApiResponseDto.<Void>builder()
                .status(404)
                .message(ex.getMessage())
                .build());
    }

    // Handles duplicate email
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<UserApiResponseDto<Void>> handleEmailExists(EmailAlreadyExistsException ex) {
        log.warn("Duplicate email: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(UserApiResponseDto.<Void>builder()
                .status(409)
                .message(ex.getMessage())
                .build());
    }

    // Handles invalid password
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<UserApiResponseDto<Void>> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Invalid password attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UserApiResponseDto.<Void>builder()
                .status(401)
                .message(ex.getMessage())
                .build());
    }

    // Catches everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<UserApiResponseDto<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UserApiResponseDto.<Void>builder()
                .status(500)
                .message("Something went wrong")
                .build());
    }
}
