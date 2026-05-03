package global_pass.bookings;

import java.util.List;

public interface IBookingService {
    List<BookingResponseDto> getAllBookings();
    List<BookingResponseDto> getAllBookingsByUser();
    BookingResponseDto getBookingById(String id);
    BookingResponseDto createBooking(BookingRequestDto request);
    BookingResponseDto updateBooking(String id, BookingRequestDto request);
    void deleteBooking(String id);
}
