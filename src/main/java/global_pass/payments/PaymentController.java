package global_pass.payments;

import global_pass.users.UserApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // User uploads payment proof
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserApiResponseDto<Void>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") String productId,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "note", required = false) String note) {

        PaymentResponseDto payment = paymentService.uploadPayment(userId, productId, file, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserApiResponseDto.<Void>builder()
                .status(201)
                .message("Payment proof uploaded")
                .build());
    }

    // User views own payments
    @GetMapping("/my/{userId}")
    public ResponseEntity<UserApiResponseDto<List<PaymentResponseDto>>> getMyPayments(@PathVariable Long userId) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByUser(userId);
        return ResponseEntity.ok(UserApiResponseDto.<List<PaymentResponseDto>>builder()
                .status(200)
                .message("Payments retrieved")
                .data(payments)
                .build());
    }

    // Admin views all payments
    @GetMapping
    public ResponseEntity<UserApiResponseDto<List<PaymentResponseDto>>> getAllPayments() {
        List<PaymentResponseDto> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(UserApiResponseDto.<List<PaymentResponseDto>>builder()
                .status(200)
                .message("All payments retrieved")
                .data(payments)
                .build());
    }

    // Admin updates payment status (approve/reject)
    @PutMapping("/{id}/status")
    public ResponseEntity<UserApiResponseDto<PaymentResponseDto>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequest request) {

        PaymentResponseDto payment = paymentService.updateStatus(id, request.getStatus(), request.getAdminNote());
        return ResponseEntity.ok(UserApiResponseDto.<PaymentResponseDto>builder()
                .status(200)
                .message("Payment status updated to " + request.getStatus())
                .data(payment)
                .build());
    }

    // Admin downloads the uploaded file
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        PaymentEntity payment = paymentService.getPaymentEntity(id);
        Resource resource = paymentService.getFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(payment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + payment.getOriginalFileName() + "\"")
                .body(resource);
    }
}
