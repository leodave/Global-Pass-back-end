package global_pass.payments;

import global_pass.config.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.StatusResultMatchersExtensionsKt.isEqualTo;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentResponseDto responseDto;
    private PaymentRequestDto requestDto;

    @BeforeEach
    void setUp() {
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

        requestDto = new PaymentRequestDto();
        requestDto.setBookingId("booking-1");
        requestDto.setAmount(100.0);
        requestDto.setCurrency("USD");
    }

    // ─── POST /api/payments ─────────────────────────────

    @Test
    @WithMockUser
    void createPayment_shouldReturn201() {
        when(paymentService.createPayment(any(PaymentRequestDto.class)))
                .thenReturn(responseDto);

        var result = paymentController.createPayment(requestDto);

        assertThat(result.getBody().getData().getStatus()).isEqualTo(201);
        assertThat(result.getBody().getData().getId()).isEqualTo("payment-1");
        assertThat(result.getBody().getData().getCurrency()).isEqualTo("USD");
    }

    @Test
    void createPayment_shouldThrowException_whenInvalidRequest() {
        requestDto.setCurrency("INVALID");

        when(paymentService.createPayment(any()))
                .thenThrow(new IllegalArgumentException("Invalid currency"));

        assertThatThrownBy(() -> paymentController.createPayment(requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── GET /api/payments/my ─────────────────────────────

    @Test
    @WithMockUser
    void getMyPayments_shouldReturnList() {
        when(paymentService.getMyPayments())
                .thenReturn(List.of(responseDto));

        var result = paymentController.getMyPayments();

        assertThat(result.getBody().getData()).hasSize(1);
        assertThat(result.getBody().getData().get(0).getId()).isEqualTo("payment-1");
    }

    // ─── GET /api/payments/{id} ─────────────────────────────

    @Test
    @WithMockUser
    void getPayment_shouldReturnPayment() {
        when(paymentService.getPayment("payment-1"))
                .thenReturn(responseDto);

        doNothing().when(securityUtil).verifyOwnershipOrAdmin(1L);

        var result = paymentController.getPayment("payment-1");

        assertThat(result.getBody().getData().getId()).isEqualTo("payment-1");
    }

    @Test
    void getPayment_shouldThrow_whenUnauthorized() {
        when(paymentService.getPayment("payment-1"))
                .thenReturn(responseDto);

        doThrow(new AccessDeniedException("Forbidden"))
                .when(securityUtil).verifyOwnershipOrAdmin(1L);

        assertThatThrownBy(() -> paymentController.getPayment("payment-1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ─── GET /api/payments ─────────────────────────────

    @Test
    void getAllPayments_shouldReturnList_forAdmin() {
        when(paymentService.getAllPayments())
                .thenReturn(List.of(responseDto));

        var result = paymentController.getAllPayments();

        assertThat(result.getBody().getData()).hasSize(1);
        assertThat(result.getBody().getData().get(0).getId()).isEqualTo("payment-1");
    }

    @Test
    void getAllPayments_shouldThrow_forNonAdmin() {
        doThrow(new AccessDeniedException("Forbidden"))
                .when(securityUtil).verifyOwnershipOrAdmin(any());

        assertThatThrownBy(() -> paymentController.getAllPayments())
                .isInstanceOf(AccessDeniedException.class);
    }

    // ─── PUT /api/payments/{id}/status ─────────────────────────────

    @Test
    void updateStatus_shouldReturnUpdatedPayment() {
        StatusUpdateRequestDto statusRequest = new StatusUpdateRequestDto();
        statusRequest.setStatus(PaymentStatus.VERIFIED);

        when(paymentService.updateStatus(eq("payment-1"), any()))
                .thenReturn(responseDto);

        var result = paymentController.updateStatus("payment-1", statusRequest);

        assertThat(result.getBody().getData().getId()).isEqualTo("payment-1");
    }

    @Test
    void updateStatus_shouldThrow_whenStatusNull() {
        StatusUpdateRequestDto statusRequest = new StatusUpdateRequestDto();

        when(paymentService.updateStatus(eq("payment-1"), any()))
                .thenThrow(new IllegalArgumentException("Status cannot be null"));

        assertThatThrownBy(() ->
                paymentController.updateStatus("payment-1", statusRequest)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    // ─── DELETE /api/payments/{id} ─────────────────────────────

    @Test
    void cancelPayment_shouldSucceed() {
        when(paymentService.getPayment("payment-1"))
                .thenReturn(responseDto);

        doNothing().when(securityUtil).verifyOwnershipOrAdmin(1L);
        doNothing().when(paymentService).cancelPayment("payment-1");

        var result = paymentController.cancelPayment("payment-1");

        assertThat(result.getBody().getMessage()).isEqualTo("Payment cancelled");
    }

    @Test
    void cancelPayment_shouldThrow_whenUnauthorized() {
        when(paymentService.getPayment("payment-1"))
                .thenReturn(responseDto);

        doThrow(new AccessDeniedException("Forbidden"))
                .when(securityUtil).verifyOwnershipOrAdmin(1L);

        assertThatThrownBy(() ->
                paymentController.cancelPayment("payment-1")
        ).isInstanceOf(AccessDeniedException.class);
    }
}
