package global_pass.bookings;

import java.util.List;

import global_pass.config.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("public/api/v1/users/{userId}/bookings")
public class BookingsController {

    private IBookingService bookingService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<BookingResponseDto>>> getAllBookings(
            @PathVariable Long userId) {
        List<BookingResponseDto> bookings = bookingService.getAllBookingsByUser(userId);
        return ResponseEntity.ok(ApiResponseDto.<List<BookingResponseDto>>builder()
                .status(200)
                .message("Bookings retrieved")
                .data(bookings)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BookingResponseDto>> getBookingById(
            @PathVariable Long userId,
            @PathVariable String id) {
        BookingResponseDto booking = bookingService.getBookingById(userId, id);
        return ResponseEntity.ok(ApiResponseDto.<BookingResponseDto>builder()
                .status(200)
                .message("Booking retrieved")
                .data(booking)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<BookingResponseDto>> createBooking(
            @PathVariable Long userId,
            @Valid @RequestBody BookingRequestDto request) {
        BookingResponseDto booking = bookingService.createBooking(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.<BookingResponseDto>builder()
                        .status(201)
                        .message("Booking created")
                        .data(booking)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BookingResponseDto>> updateBooking(
            @PathVariable Long userId,
            @PathVariable String id,
            @Valid @RequestBody BookingRequestDto request) {
        BookingResponseDto booking = bookingService.updateBooking(userId, id, request);
        return ResponseEntity.ok(ApiResponseDto.<BookingResponseDto>builder()
                .status(200)
                .message("Booking updated")
                .data(booking)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBooking(
            @PathVariable Long userId,
            @PathVariable String id) {
        bookingService.deleteBooking(userId, id);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Booking deleted")
                .data(null)
                .build());
    }
}
