package global_pass.exception.globalException;

import global_pass.exception.customBookingException.BookingNotFoundException;
import global_pass.exception.customUserException.EmailAlreadyExistsException;
import global_pass.exception.customUserException.InvalidPasswordException;
import global_pass.exception.customUserException.UserNotFoundException;
import global_pass.config.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles validation errors (@NotBlank, @Email, @Size etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Validation error: {}", message);
        return ResponseEntity.badRequest().body(ApiResponseDto.<Void>builder()
                .status(400)
                .message(message)
                .build());
    }

    // Handles user not found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.<Void>builder()
                .status(404)
                .message(ex.getMessage())
                .build());
    }

    // Handles duplicate email
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleEmailExists(EmailAlreadyExistsException ex) {
        log.warn("Duplicate email: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.<Void>builder()
                .status(409)
                .message(ex.getMessage())
                .build());
    }

    // Handles invalid password
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Invalid password attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.<Void>builder()
                .status(401)
                .message(ex.getMessage())
                .build());
    }

    // 2. Handles malformed JSON body (missing required fields, wrong types)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableMessage(
            HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Malformed request body");
    }

    // Handles your BookingNotFoundException
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<String> handleBookingNotFound(
            BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    // Catches everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDto.<Void>builder()
                .status(500)
                .message("Something went wrong")
                .build());
    }
}
