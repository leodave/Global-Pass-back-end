package global_pass.payments;

import global_pass.config.ApiResponseDto;
import global_pass.config.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtil securityUtil;

    // ✅ Create payment — userId extracted from JWT in service
    @PostMapping
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> createPayment(
            @Valid @RequestBody PaymentRequestDto request) {
        PaymentResponseDto payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<PaymentResponseDto>builder()
                .status(201)
                .message("Payment created successfully")
                .data(payment)
                .build());
    }

    // ✅ Get my payments — userId from JWT, no path variable needed
    @GetMapping("/my")
    public ResponseEntity<ApiResponseDto<List<PaymentResponseDto>>> getMyPayments() {
        List<PaymentResponseDto> payments = paymentService.getMyPayments();
        return ResponseEntity.ok(ApiResponseDto.<List<PaymentResponseDto>>builder()
                .status(200)
                .message("Payments retrieved")
                .data(payments)
                .build());
    }

    // ✅ Get single payment — verify ownership or admin
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> getPayment(@PathVariable String id) {
        PaymentResponseDto payment = paymentService.getPayment(id);
        securityUtil.verifyOwnershipOrAdmin(payment.getUserId()); // ← verify after fetch
        return ResponseEntity.ok(ApiResponseDto.<PaymentResponseDto>builder()
                .status(200)
                .message("Payment retrieved")
                .data(payment)
                .build());
    }

    // ✅ Get all payments — admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<List<PaymentResponseDto>>> getAllPayments() {
        List<PaymentResponseDto> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponseDto.<List<PaymentResponseDto>>builder()
                .status(200)
                .message("All payments retrieved")
                .data(payments)
                .build());
    }

    // ✅ Update status — admin only
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequestDto request) {
        PaymentResponseDto payment = paymentService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponseDto.<PaymentResponseDto>builder()
                .status(200)
                .message("Payment status updated to " + request.getStatus())
                .data(payment)
                .build());
    }

    // ✅ Cancel/delete payment — only owner
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> cancelPayment(@PathVariable String id) {
        PaymentResponseDto payment = paymentService.getPayment(id);
        paymentService.cancelPayment(id);
        securityUtil.verifyOwnershipOrAdmin(payment.getUserId());
        paymentService.cancelPayment(id);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Payment cancelled")
                .build());
    }
}
