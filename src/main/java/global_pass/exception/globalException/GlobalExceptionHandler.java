package global_pass.exception.globalException;

import java.util.Arrays;
import java.util.stream.Collectors;

import global_pass.exception.customBookingException.BookingNotFoundException;
import global_pass.exception.customUserException.EmailAlreadyExistsException;
import global_pass.exception.customUserException.InvalidPasswordException;
import global_pass.exception.customUserException.UserNotFoundException;
import global_pass.config.ApiResponseDto;
import global_pass.payments.PaymentStatus;
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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.<Void>builder()
                .status(404)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleEmailExists(EmailAlreadyExistsException ex) {
        log.warn("Duplicate email: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.<Void>builder()
                .status(409)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Invalid password attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.<Void>builder()
                .status(401)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiResponseDto.<Void>builder()
                .status(400)
                .message("Malformed request body")
                .build());
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.<Void>builder()
                .status(404)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponseDto.<Void>builder()
                .status(400)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidEnum(HttpMessageNotReadableException ex) {
        String message = "Invalid value. Accepted values are: "
                + Arrays.stream(PaymentStatus.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(ApiResponseDto.<Void>builder()
                .status(400)
                .message(message)
                .build());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMaxUploadSize(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        log.warn("File too large: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponseDto.<Void>builder()
                .status(400)
                .message("File exceeds maximum upload size of 10MB")
                .build());
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleNoResource(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.<Void>builder()
                .status(404)
                .message("Not found")
                .build());
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponseDto.<Void>builder()
                .status(405)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDto.<Void>builder()
                .status(500)
                .message("Something went wrong")
                .build());
    }
}
