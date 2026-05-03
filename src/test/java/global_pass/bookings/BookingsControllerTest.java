package global_pass.bookings;

import global_pass.config.ApiResponseDto;
import global_pass.config.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingsControllerTest {

    @Mock
    private IBookingService bookingService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private BookingsController bookingsController;

    private static final Long USER_ID = 1L;
    private static final String BOOKING_ID = "booking-uuid-1";

    private BookingRequestDto validRequest;
    private BookingResponseDto bookingResponse;

    @BeforeEach
    void setUp() {
        validRequest = BookingRequestDto.builder()
                .name("Test Booking")
                .description("Test Description")
                .pageLink("https://example.com")
                .loginUsername("user123")
                .loginPassword("pass123")
                .otherDetails("Some details")
                .build();

        bookingResponse = BookingResponseDto.builder()
                .id(BOOKING_ID)
                .userId(USER_ID)
                .name("Test Booking")
                .description("Test Description")
                .pageLink("https://example.com")
                .loginUsername("user123")
                .loginPassword("pass123")
                .otherDetails("Some details")
                .build();
    }

    // ──────────────────────────────────────────────
    // GET /api/bookings (admin)
    // ──────────────────────────────────────────────

    @Test
    void getAllBookingsAdmin_shouldReturn200WithList() {
        when(bookingService.getAllBookings()).thenReturn(List.of(bookingResponse));

        ResponseEntity<ApiResponseDto<List<BookingResponseDto>>> response =
                bookingsController.getAllBookingsAdmin();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("All bookings retrieved");
        assertThat(response.getBody().getData()).hasSize(1);

        verify(bookingService, times(1)).getAllBookings();
        verifyNoInteractions(securityUtil);
    }

    // ──────────────────────────────────────────────
    // GET /api/v1/users/{userId}/bookings
    // ──────────────────────────────────────────────

    @Test
    void getAllBookings_shouldReturn200WithList() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        when(bookingService.getAllBookingsByUser()).thenReturn(List.of(bookingResponse));

        ResponseEntity<ApiResponseDto<List<BookingResponseDto>>> response =
                bookingsController.getAllBookings(USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("Bookings retrieved");
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getId()).isEqualTo(BOOKING_ID);

        verify(securityUtil, times(1)).verifyOwnershipOrAdmin(USER_ID);
        verify(bookingService, times(1)).getAllBookingsByUser();
    }

    @Test
    void getAllBookings_shouldReturnEmptyList() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        when(bookingService.getAllBookingsByUser()).thenReturn(List.of());

        ResponseEntity<ApiResponseDto<List<BookingResponseDto>>> response =
                bookingsController.getAllBookings(USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEmpty();

        verify(bookingService, times(1)).getAllBookingsByUser();
    }

    @Test
    void getAllBookings_shouldThrow_whenAccessDenied() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        bookingsController.getAllBookings(USER_ID))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(bookingService, never()).getAllBookingsByUser();
    }

    // ──────────────────────────────────────────────
    // GET /api/v1/users/{userId}/bookings/{id}
    // ──────────────────────────────────────────────

    @Test
    void getBookingById_shouldReturn200() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        when(bookingService.getBookingById(BOOKING_ID)).thenReturn(bookingResponse);

        ResponseEntity<ApiResponseDto<BookingResponseDto>> response =
                bookingsController.getBookingById(USER_ID, BOOKING_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("Booking retrieved");
        assertThat(response.getBody().getData().getId()).isEqualTo(BOOKING_ID);

        verify(securityUtil, times(1)).verifyOwnershipOrAdmin(USER_ID);
        verify(bookingService, times(1)).getBookingById(BOOKING_ID);
    }

    @Test
    void getBookingById_shouldThrow_whenAccessDenied() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        bookingsController.getBookingById(USER_ID, BOOKING_ID))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(bookingService, never()).getBookingById(any());
    }

    // ──────────────────────────────────────────────
    // POST /api/v1/users/{userId}/bookings
    // ──────────────────────────────────────────────

    @Test
    void createBooking_shouldReturn201() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        when(bookingService.createBooking(any(BookingRequestDto.class)))
                .thenReturn(bookingResponse);

        ResponseEntity<ApiResponseDto<BookingResponseDto>> response =
                bookingsController.createBooking(USER_ID, validRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(201);
        assertThat(response.getBody().getMessage()).isEqualTo("Booking created");
        assertThat(response.getBody().getData().getId()).isEqualTo(BOOKING_ID);

        verify(securityUtil, times(1)).verifyOwnershipOrAdmin(USER_ID);
        verify(bookingService, times(1)).createBooking(validRequest);
    }

    @Test
    void createBooking_shouldThrow_whenAccessDenied() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        bookingsController.createBooking(USER_ID, validRequest))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(bookingService, never()).createBooking(any());
    }

    // ──────────────────────────────────────────────
    // PUT /api/v1/users/{userId}/bookings/{id}
    // ──────────────────────────────────────────────

    @Test
    void updateBooking_shouldReturn200() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        when(bookingService.updateBooking(eq(BOOKING_ID), any(BookingRequestDto.class)))
                .thenReturn(bookingResponse);

        ResponseEntity<ApiResponseDto<BookingResponseDto>> response =
                bookingsController.updateBooking(USER_ID, BOOKING_ID, validRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("Booking updated");
        assertThat(response.getBody().getData().getId()).isEqualTo(BOOKING_ID);

        verify(securityUtil, times(1)).verifyOwnershipOrAdmin(USER_ID);
        verify(bookingService, times(1)).updateBooking(BOOKING_ID, validRequest);
    }

    @Test
    void updateBooking_shouldThrow_whenAccessDenied() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        bookingsController.updateBooking(USER_ID, BOOKING_ID, validRequest))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(bookingService, never()).updateBooking(any(), any());
    }

    // ──────────────────────────────────────────────
    // DELETE /api/v1/users/{userId}/bookings/{id}
    // ──────────────────────────────────────────────

    @Test
    void deleteBooking_shouldReturn200() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        doNothing().when(bookingService).deleteBooking(BOOKING_ID);

        ResponseEntity<ApiResponseDto<Void>> response =
                bookingsController.deleteBooking(USER_ID, BOOKING_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("Booking deleted");
        assertThat(response.getBody().getData()).isNull();

        verify(securityUtil, times(1)).verifyOwnershipOrAdmin(USER_ID);
        verify(bookingService, times(1)).deleteBooking(BOOKING_ID);
    }

    @Test
    void deleteBooking_shouldThrow_whenAccessDenied() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        bookingsController.deleteBooking(USER_ID, BOOKING_ID))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(bookingService, never()).deleteBooking(any());
    }
}
