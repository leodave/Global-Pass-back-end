package global_pass.bookings;

import java.util.List;

import global_pass.exception.customBookingException.BookingNotFoundException;
import global_pass.users.User;
import global_pass.users.UserRepository;
import global_pass.exception.customUserException.UserNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class BookingService implements IBookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;

    @Override
    public List<BookingResponseDto> getAllBookings() {
        log.info("Fetching all bookings");
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toResponseDto)
                .toList();
    }

    @Override
    public BookingResponseDto getBookingByIdPublic(String id) {
        log.info("Fetching booking with id: {}", id);
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Booking not found with id: {}", id);
                    return new BookingNotFoundException(id);
                });
        return bookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllBookingsByUser(Long userId) {
        log.info("Fetching all bookings for userId: {}", userId);
        List<BookingResponseDto> bookings = bookingRepository.findAllByUserId(userId)
                .stream()
                .map(bookingMapper::toResponseDto)
                .toList();
        log.info("Found {} bookings for userId: {}", bookings.size(), userId);
        return bookings;
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, String id) {
        log.info("Fetching booking with id: {} for userId: {}", id, userId);
        BookingEntity booking = findBookingOrThrow(userId, id);
        return bookingMapper.toResponseDto(booking);
    }

    @Override
    public BookingResponseDto createBooking(Long userId, BookingRequestDto request) {
        log.info("Creating booking for userId: {}", userId);
        User user = findUserOrThrow(userId);
        BookingEntity entity = bookingMapper.toEntity(request);
        entity.setUser(user);
        BookingEntity saved = bookingRepository.save(entity);
        log.info("Booking created successfully with id: {} for userId: {}", saved.getId(), userId);
        return bookingMapper.toResponseDto(saved);
    }

    @Override
    public BookingResponseDto updateBooking(Long userId, String id, BookingRequestDto request) {
        log.info("Updating booking with id: {} for userId: {}", id, userId);
        BookingEntity existing = findBookingOrThrow(userId, id);
        bookingMapper.updateEntityFromRequest(request, existing);
        BookingEntity saved = bookingRepository.save(existing);
        log.info("Booking updated successfully with id: {} for userId: {}", saved.getId(), userId);
        return bookingMapper.toResponseDto(saved);
    }

    @Override
    public void deleteBooking(Long userId, String id) {
        log.info("Deleting booking with id: {} for userId: {}", id, userId);
        findBookingOrThrow(userId, id);
        bookingRepository.deleteById(id);
        log.info("Booking deleted successfully with id: {} for userId: {}", id, userId);
    }

    private BookingEntity findBookingOrThrow(Long userId, String id) {
        return bookingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Booking not found with id: {} for userId: {}", id, userId);
                    return new BookingNotFoundException(id);
                });
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new UserNotFoundException(userId.toString());
                });
    }
}
