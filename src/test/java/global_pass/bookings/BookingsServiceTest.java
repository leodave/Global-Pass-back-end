package global_pass.bookings;

import global_pass.config.SecurityUtil;
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
class BookingsServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private UserRepository userRepository;
    @Mock private SecurityUtil securityUtil; // ← added

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
        entity.setOtherDetails("4K plan");

        responseDto = BookingResponseDto.builder()
                .id(BOOKING_ID)
                .userId(USER_ID)
                .name("Netflix")
                .description("Streaming")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .otherDetails("4K plan")
                .build();

        requestDto = BookingRequestDto.builder()
                .name("Netflix")
                .description("Streaming")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .otherDetails("4K plan")
                .build();

        // ← default for all tests — override per test if needed
        lenient().when(securityUtil.getAuthenticatedUserId()).thenReturn(USER_ID);
    }

    // ──────────────────────────────────────────────
    // getAllBookings (admin)
    // ──────────────────────────────────────────────

    @Test
    void getAllBookings_returnsAllBookings() {
        when(bookingRepository.findAllWithUser()).thenReturn(List.of(entity));
        when(bookingMapper.toResponseDto(entity)).thenReturn(responseDto); // ✅ REQUIRED

        List<BookingResponseDto> result = bookingService.getAllBookings();

        assertThat(result).hasSize(1).containsExactly(responseDto);
        verify(bookingRepository).findAllWithUser();
        verify(bookingMapper).toResponseDto(entity); // optional but good
        verifyNoInteractions(securityUtil);
    }

    // ──────────────────────────────────────────────
    // getAllBookingsByUser
    // ──────────────────────────────────────────────

    @Test
    void getAllBookingsByUser_returnsListOfDtos() {
        when(bookingRepository.findAllByUserId(USER_ID)).thenReturn(List.of(entity));
        when(bookingMapper.toResponseDto(entity)).thenReturn(responseDto);

        List<BookingResponseDto> result = bookingService.getAllBookingsByUser();

        assertThat(result).hasSize(1).containsExactly(responseDto);
        verify(securityUtil).getAuthenticatedUserId();
        verify(bookingRepository).findAllByUserId(USER_ID);
    }

    @Test
    void getAllBookingsByUser_returnsEmptyList_whenNoBookings() {
        when(bookingRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        List<BookingResponseDto> result = bookingService.getAllBookingsByUser();

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

        BookingResponseDto result = bookingService.getBookingById(BOOKING_ID);

        assertThat(result).isEqualTo(responseDto);
        verify(securityUtil).getAuthenticatedUserId();
        verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
    }

    @Test
    void getBookingById_throws_whenNotFound() {
        when(bookingRepository.findByIdAndUserId("bad-id", USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById("bad-id"))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("bad-id");
    }

    @Test
    void getBookingById_throws_whenBookingBelongsToOtherUser() {
        // simulate different user authenticated
        when(securityUtil.getAuthenticatedUserId()).thenReturn(99L);
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(BOOKING_ID))
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

        BookingResponseDto result = bookingService.createBooking(requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(securityUtil).getAuthenticatedUserId();
        verify(userRepository).findById(USER_ID);
        verify(bookingRepository).save(entity);
    }

    @Test
    void createBooking_setsUserOnEntity() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(bookingMapper.toEntity(requestDto)).thenReturn(entity);
        when(bookingRepository.save(entity)).thenReturn(entity);
        when(bookingMapper.toResponseDto(entity)).thenReturn(responseDto);

        bookingService.createBooking(requestDto);

        assertThat(entity.getUser()).isEqualTo(user);
    }

    @Test
    void createBooking_throws_whenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(requestDto))
                .isInstanceOf(UserNotFoundException.class);

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

        BookingResponseDto result = bookingService.updateBooking(BOOKING_ID, requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(securityUtil).getAuthenticatedUserId();
        verify(bookingMapper).updateEntityFromRequest(requestDto, entity);
        verify(bookingRepository).save(entity);
    }

    @Test
    void updateBooking_throws_whenNotFound() {
        when(bookingRepository.findByIdAndUserId("bad-id", USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBooking("bad-id", requestDto))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("bad-id");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBooking_throws_whenBookingBelongsToOtherUser() {
        when(securityUtil.getAuthenticatedUserId()).thenReturn(99L);
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBooking(BOOKING_ID, requestDto))
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

        bookingService.deleteBooking(BOOKING_ID);

        verify(securityUtil).getAuthenticatedUserId();
        verify(bookingRepository).deleteById(BOOKING_ID);
    }

    @Test
    void deleteBooking_throws_whenNotFound() {
        when(bookingRepository.findByIdAndUserId("bad-id", USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteBooking("bad-id"))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("bad-id");

        verify(bookingRepository, never()).deleteById(any());
    }

    @Test
    void deleteBooking_throws_whenBookingBelongsToOtherUser() {
        when(securityUtil.getAuthenticatedUserId()).thenReturn(99L);
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteBooking(BOOKING_ID))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining(BOOKING_ID);

        verify(bookingRepository, never()).deleteById(any());
    }
}
