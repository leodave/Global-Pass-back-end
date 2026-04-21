package global_pass.bookings;

import java.util.List;

public interface IBookingService {
    List<BookingResponseDto> getAllBookingsByUser(Long userId);
    BookingResponseDto getBookingById(Long userId, String id);
    BookingResponseDto createBooking(Long userId, BookingRequestDto request);
    BookingResponseDto updateBooking(Long userId, String id, BookingRequestDto request);
    void deleteBooking(Long userId, String id);
}
