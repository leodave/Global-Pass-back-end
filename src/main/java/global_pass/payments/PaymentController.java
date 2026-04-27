package global_pass.payments;

import global_pass.config.ApiResponseDto;
import global_pass.users.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    private Long getAuthenticatedUserId() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getId();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void verifyPaymentOwnershipOrAdmin(String paymentId) {
        if (isAdmin()) return;
        PaymentEntity payment = paymentService.getPaymentEntity(paymentId);
        if (!payment.getUserId().equals(getAuthenticatedUserId())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bookingId") String bookingId,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "amount", required = false) Double amount,
            @RequestParam(value = "note", required = false) String note) {

        if (!isAdmin() && !getAuthenticatedUserId().equals(userId)) {
            throw new AccessDeniedException("Cannot upload payment for another user");
        }
        PaymentResponseDto payment = paymentService.uploadPayment(userId, bookingId, file, amount, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<PaymentResponseDto>builder()
                .status(201)
                .message("Payment proof uploaded")
                .data(payment)
                .build());
    }

    @GetMapping("/my/{userId}")
    public ResponseEntity<ApiResponseDto<List<PaymentResponseDto>>> getMyPayments(@PathVariable Long userId) {
        if (!isAdmin() && !getAuthenticatedUserId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        List<PaymentResponseDto> payments = paymentService.getPaymentsByUser(userId);
        return ResponseEntity.ok(ApiResponseDto.<List<PaymentResponseDto>>builder()
                .status(200)
                .message("Payments retrieved")
                .data(payments)
                .build());
    }

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

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequest request) {

        PaymentResponseDto payment = paymentService.updateStatus(id, request.getStatus(), request.getAdminNote());
        return ResponseEntity.ok(ApiResponseDto.<PaymentResponseDto>builder()
                .status(200)
                .message("Payment status updated to " + request.getStatus())
                .data(payment)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> cancelPayment(@PathVariable String id) {
        verifyPaymentOwnershipOrAdmin(id);
        paymentService.cancelPayment(id);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Payment cancelled")
                .build());
    }

    @PutMapping(value = "/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> reuploadFile(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        verifyPaymentOwnershipOrAdmin(id);
        PaymentResponseDto payment = paymentService.reuploadFile(id, file);
        return ResponseEntity.ok(ApiResponseDto.<PaymentResponseDto>builder()
                .status(200)
                .message("File replaced")
                .data(payment)
                .build());
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        verifyPaymentOwnershipOrAdmin(id);
        PaymentEntity payment = paymentService.getPaymentEntity(id);
        Resource resource = paymentService.getFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(payment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + payment.getOriginalFileName() + "\"")
                .body(resource);
    }
}
