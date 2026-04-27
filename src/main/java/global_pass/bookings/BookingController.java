package global_pass.bookings;

import global_pass.config.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRepository bookingRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<List<BookingEntity>>> getAllBookings() {
        List<BookingEntity> bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(ApiResponseDto.<List<BookingEntity>>builder()
                .status(200)
                .message("All bookings retrieved")
                .data(bookings)
                .build());
    }
}
