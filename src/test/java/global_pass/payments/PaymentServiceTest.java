package global_pass.payments;

import global_pass.bookings.BookingEntity;
import global_pass.bookings.BookingRepository;
import global_pass.config.SecurityUtil;
import global_pass.users.User;
import global_pass.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private BookingEntity booking;
    private PaymentEntity payment;
    private PaymentRequestDto request;
    private PaymentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("dave@mail.com");
        user.setName("Dave");

        booking = new BookingEntity();
        booking.setId("booking-1");
        booking.setUser(user);

        payment = new PaymentEntity();
        payment.setId("payment-1");
        payment.setUser(user);
        payment.setBooking(booking);
        payment.setAmount(100.0);
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);

        request = new PaymentRequestDto();
        request.setBookingId("booking-1");
        request.setAmount(100.0);
        request.setCurrency("USD");

        responseDto = PaymentResponseDto.builder()
                .id("payment-1")
                .userId(1L)
                .userName("Dave")
                .userEmail("dave@mail.com")
                .bookingId("booking-1")
                .amount(100.0)
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .build();
    }

    // ─── createPayment ───────────────────────────────────────────

    @Test
    void createPayment_shouldCreateSuccessfully() {
        when(securityUtil.getAuthenticatedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingId("booking-1")).thenReturn(false);
        when(paymentMapper.toEntity(request, user, booking)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toResponseDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.createPayment(request);

        assertThat(result).isEqualTo(responseDto);
        verify(paymentRepository).save(payment);
    }

    @Test
    void createPayment_shouldThrow_whenBookingNotFound() {
        when(securityUtil.getAuthenticatedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void createPayment_shouldThrow_whenBookingBelongsToDifferentUser() {
        User otherUser = new User();
        otherUser.setId(99L);
        booking.setUser(otherUser);

        when(securityUtil.getAuthenticatedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createPayment_shouldThrow_whenPaymentAlreadyExists() {
        when(securityUtil.getAuthenticatedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findById("booking-1")).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingId("booking-1")).thenReturn(true);

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    // ─── getMyPayments ───────────────────────────────────────────

    @Test
    void getMyPayments_shouldReturnUserPayments() {
        when(securityUtil.getAuthenticatedUserId()).thenReturn(1L);
        when(paymentRepository.findAllByUserIdWithDetails(1L)).thenReturn(List.of(payment));
        when(paymentMapper.toResponseDto(payment)).thenReturn(responseDto);

        List<PaymentResponseDto> result = paymentService.getMyPayments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDto);
    }

    @Test
    void getMyPayments_shouldReturnEmpty_whenNoPayments() {
        when(securityUtil.getAuthenticatedUserId()).thenReturn(1L);
        when(paymentRepository.findAllByUserIdWithDetails(1L)).thenReturn(List.of());

        List<PaymentResponseDto> result = paymentService.getMyPayments();

        assertThat(result).isEmpty();
    }

    // ─── getPayment ───────────────────────────────────────────

    @Test
    void getPayment_shouldReturnPayment() {
        when(paymentRepository.findByIdWithDetails("payment-1")).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponseDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.getPayment("payment-1");

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    void getPayment_shouldThrow_whenNotFound() {
        when(paymentRepository.findByIdWithDetails("payment-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment("payment-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
    }

    // ─── getAllPayments ───────────────────────────────────────────

    @Test
    void getAllPayments_shouldReturnAll() {
        when(paymentRepository.findAllWithDetails()).thenReturn(List.of(payment));
        when(paymentMapper.toResponseDto(payment)).thenReturn(responseDto);

        List<PaymentResponseDto> result = paymentService.getAllPayments();

        assertThat(result).hasSize(1);
    }

    // ─── updateStatus ───────────────────────────────────────────

    @Test
    void updateStatus_shouldUpdateSuccessfully() {
        StatusUpdateRequestDto statusRequest = new StatusUpdateRequestDto();
        statusRequest.setStatus(PaymentStatus.VERIFIED);
        statusRequest.setAdminNote("looks good");

        when(paymentRepository.findById("payment-1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toResponseDto(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.updateStatus("payment-1", statusRequest);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.VERIFIED);
        assertThat(payment.getAdminNote()).isEqualTo("looks good");
        verify(paymentRepository).save(payment);
    }

    @Test
    void updateStatus_shouldThrow_whenNotFound() {
        StatusUpdateRequestDto statusRequest = new StatusUpdateRequestDto();
        statusRequest.setStatus(PaymentStatus.VERIFIED);

        when(paymentRepository.findById("payment-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.updateStatus("payment-1", statusRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
    }

    // ─── cancelPayment ───────────────────────────────────────────

    @Test
    void cancelPayment_shouldDeleteSuccessfully() {
        when(paymentRepository.findById("payment-1")).thenReturn(Optional.of(payment));

        paymentService.cancelPayment("payment-1");

        verify(paymentRepository).delete(payment);
    }

    @Test
    void cancelPayment_shouldThrow_whenPaymentIsVerified() {
        payment.setStatus(PaymentStatus.VERIFIED);
        when(paymentRepository.findById("payment-1")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.cancelPayment("payment-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot cancel a verified payment");
    }

    @Test
    void cancelPayment_shouldThrow_whenNotFound() {
        when(paymentRepository.findById("payment-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.cancelPayment("payment-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
    }
}
