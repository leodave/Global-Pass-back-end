package global_pass.bookings;

import global_pass.exception.customBookingException.BookingNotFoundException;
import global_pass.exception.customUserException.UserNotFoundException;
import global_pass.users.User;
import global_pass.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private UserRepository userRepository;

    @InjectMocks private BookingService bookingService;

    private static final Long USER_ID = 1L;
    private static final String BOOKING_ID = "uuid-123";

    private User user;
    private BookingEntity entity;
    private BookingResponseDto responseDto;
    private BookingRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);

        entity = new BookingEntity();
        entity.setId(BOOKING_ID);
        entity.setUser(user);
        entity.setName("Netflix");
        entity.setDescription("Streaming");
        entity.setPageLink("https://netflix.com");
        entity.setLoginUsername("user@mail.com");
        entity.setLoginPassword("secret123");
        entity.setAmount(15.99);
        entity.setCurrency("USD");
        entity.setOtherDetails("4K plan");

        responseDto = BookingResponseDto.builder()
                .id(BOOKING_ID)
                .userId(USER_ID)
                .name("Netflix")
                .description("Streaming")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .otherDetails("4K plan")
                .build();

        requestDto = BookingRequestDto.builder()
                .name("Netflix")
                .description("Streaming")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .otherDetails("4K plan")
                .build();
    }

    // ──────────────────────────────────────────────
    // getAllBookingsByUser
    // ──────────────────────────────────────────────

    @Test
    void getAllBookingsByUser_returnsListOfDtos() {
        when(bookingRepository.findAllByUserId(USER_ID)).thenReturn(List.of(entity));
        when(bookingMapper.toResponseDto(entity)).thenReturn(responseDto);

        List<BookingResponseDto> result = bookingService.getAllBookingsByUser(USER_ID);

        assertThat(result).hasSize(1).containsExactly(responseDto);
        verify(bookingRepository).findAllByUserId(USER_ID);
    }

    @Test
    void getAllBookingsByUser_returnsEmptyList_whenNoBookings() {
        when(bookingRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        List<BookingResponseDto> result = bookingService.getAllBookingsByUser(USER_ID);

        assertThat(result).isEmpty();
        verifyNoInteractions(bookingMapper);
    }

    // ──────────────────────────────────────────────
    // getBookingById
    // ──────────────────────────────────────────────

    @Test
    void getBookingById_returnsDto_whenFound() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(entity));
        when(bookingMapper.toResponseDto(entity)).thenReturn(responseDto);

        BookingResponseDto result = bookingService.getBookingById(USER_ID, BOOKING_ID);

        assertThat(result).isEqualTo(responseDto);
        verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
    }

    @Test
    void getBookingById_throws_whenNotFound() {
        when(bookingRepository.findByIdAndUserId("bad-id", USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(USER_ID, "bad-id"))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("bad-id");
    }

    @Test
    void getBookingById_throws_whenBookingBelongsToOtherUser() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(99L, BOOKING_ID))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining(BOOKING_ID);
    }

    // ──────────────────────────────────────────────
    // createBooking
    // ──────────────────────────────────────────────

    @Test
    void createBooking_savesAndReturnsDto() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(bookingMapper.toEntity(requestDto)).thenReturn(entity);
        when(bookingRepository.save(entity)).thenReturn(entity);
        when(bookingMapper.toResponseDto(entity)).thenReturn(responseDto);

        BookingResponseDto result = bookingService.createBooking(USER_ID, requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(userRepository).findById(USER_ID);
        verify(bookingRepository).save(entity);
    }

    @Test
    void createBooking_setsUserOnEntity() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(bookingMapper.toEntity(requestDto)).thenReturn(entity);
        when(bookingRepository.save(entity)).thenReturn(entity);
        when(bookingMapper.toResponseDto(entity)).thenReturn(responseDto);

        bookingService.createBooking(USER_ID, requestDto);

        assertThat(entity.getUser()).isEqualTo(user); // user was set on entity
    }

    @Test
    void createBooking_throws_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(99L, requestDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(bookingRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────
    // updateBooking
    // ──────────────────────────────────────────────

    @Test
    void updateBooking_updatesAndReturnsDto() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(entity));
        when(bookingRepository.save(entity)).thenReturn(entity);
        when(bookingMapper.toResponseDto(entity)).thenReturn(responseDto);

        BookingResponseDto result = bookingService.updateBooking(USER_ID, BOOKING_ID, requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(bookingMapper).updateEntityFromRequest(requestDto, entity);
        verify(bookingRepository).save(entity);
    }

    @Test
    void updateBooking_throws_whenNotFound() {
        when(bookingRepository.findByIdAndUserId("bad-id", USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBooking(USER_ID, "bad-id", requestDto))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("bad-id");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBooking_throws_whenBookingBelongsToOtherUser() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBooking(99L, BOOKING_ID, requestDto))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining(BOOKING_ID);

        verify(bookingRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────
    // deleteBooking
    // ──────────────────────────────────────────────

    @Test
    void deleteBooking_deletesSuccessfully() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(entity));

        bookingService.deleteBooking(USER_ID, BOOKING_ID);

        verify(bookingRepository).deleteById(BOOKING_ID);
    }

    @Test
    void deleteBooking_throws_whenNotFound() {
        when(bookingRepository.findByIdAndUserId("bad-id", USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteBooking(USER_ID, "bad-id"))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("bad-id");

        verify(bookingRepository, never()).deleteById(any());
    }

    @Test
    void deleteBooking_throws_whenBookingBelongsToOtherUser() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteBooking(99L, BOOKING_ID))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining(BOOKING_ID);

        verify(bookingRepository, never()).deleteById(any());
    }
}
