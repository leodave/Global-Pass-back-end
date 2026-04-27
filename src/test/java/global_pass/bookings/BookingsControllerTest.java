package global_pass.bookings;

import global_pass.config.ApiResponseDto;
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
                .amount(99.99)
                .currency("USD")
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
                .amount(99.99)
                .currency("USD")
                .otherDetails("Some details")
                .build();
    }

    // ──────────────────────────────────────────────
    // GET /bookings
    // ──────────────────────────────────────────────

    @Test
    void getAllBookings_shouldReturn200WithList() {
        when(bookingService.getAllBookingsByUser(USER_ID)).thenReturn(List.of(bookingResponse));

        ResponseEntity<ApiResponseDto<List<BookingResponseDto>>> response =
                bookingsController.getAllBookings(USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("Bookings retrieved");
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getId()).isEqualTo(BOOKING_ID);
        assertThat(response.getBody().getData().get(0).getUserId()).isEqualTo(USER_ID);

        verify(bookingService, times(1)).getAllBookingsByUser(USER_ID);
    }

    @Test
    void getAllBookings_shouldReturnEmptyList() {
        when(bookingService.getAllBookingsByUser(USER_ID)).thenReturn(List.of());

        ResponseEntity<ApiResponseDto<List<BookingResponseDto>>> response =
                bookingsController.getAllBookings(USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEmpty();

        verify(bookingService, times(1)).getAllBookingsByUser(USER_ID);
    }

    // ──────────────────────────────────────────────
    // GET /bookings/{id}
    // ──────────────────────────────────────────────

    @Test
    void getBookingById_shouldReturn200() {
        when(bookingService.getBookingById(USER_ID, BOOKING_ID)).thenReturn(bookingResponse);

        ResponseEntity<ApiResponseDto<BookingResponseDto>> response =
                bookingsController.getBookingById(USER_ID, BOOKING_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("Booking retrieved");
        assertThat(response.getBody().getData().getId()).isEqualTo(BOOKING_ID);
        assertThat(response.getBody().getData().getUserId()).isEqualTo(USER_ID);

        verify(bookingService, times(1)).getBookingById(USER_ID, BOOKING_ID);
    }

    // ──────────────────────────────────────────────
    // POST /bookings
    // ──────────────────────────────────────────────

    @Test
    void createBooking_shouldReturn201() {
        when(bookingService.createBooking(eq(USER_ID), any(BookingRequestDto.class)))
                .thenReturn(bookingResponse);

        ResponseEntity<ApiResponseDto<BookingResponseDto>> response =
                bookingsController.createBooking(USER_ID, validRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(201);
        assertThat(response.getBody().getMessage()).isEqualTo("Booking created");
        assertThat(response.getBody().getData().getId()).isEqualTo(BOOKING_ID);
        assertThat(response.getBody().getData().getUserId()).isEqualTo(USER_ID);

        verify(bookingService, times(1)).createBooking(USER_ID, validRequest);
    }

    // ──────────────────────────────────────────────
    // PUT /bookings/{id}
    // ──────────────────────────────────────────────

    @Test
    void updateBooking_shouldReturn200() {
        when(bookingService.updateBooking(eq(USER_ID), eq(BOOKING_ID), any(BookingRequestDto.class)))
                .thenReturn(bookingResponse);

        ResponseEntity<ApiResponseDto<BookingResponseDto>> response =
                bookingsController.updateBooking(USER_ID, BOOKING_ID, validRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("Booking updated");
        assertThat(response.getBody().getData().getId()).isEqualTo(BOOKING_ID);

        verify(bookingService, times(1)).updateBooking(USER_ID, BOOKING_ID, validRequest);
    }

    // ──────────────────────────────────────────────
    // DELETE /bookings/{id}
    // ──────────────────────────────────────────────

    @Test
    void deleteBooking_shouldReturn200() {
        doNothing().when(bookingService).deleteBooking(USER_ID, BOOKING_ID);

        ResponseEntity<ApiResponseDto<Void>> response =
                bookingsController.deleteBooking(USER_ID, BOOKING_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("Booking deleted");
        assertThat(response.getBody().getData()).isNull();

        verify(bookingService, times(1)).deleteBooking(USER_ID, BOOKING_ID);
    }
}
