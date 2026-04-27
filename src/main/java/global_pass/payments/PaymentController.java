package global_pass.payments;

import global_pass.config.ApiResponseDto;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<PaymentResponseDto>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bookingId") String bookingId,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "note", required = false) String note) {

        PaymentResponseDto payment = paymentService.uploadPayment(userId, bookingId, file, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<PaymentResponseDto>builder()
                .status(201)
                .message("Payment proof uploaded")
                .data(payment)
                .build());
    }

    @GetMapping("/my/{userId}")
    public ResponseEntity<ApiResponseDto<List<PaymentResponseDto>>> getMyPayments(@PathVariable Long userId) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByUser(userId);
        return ResponseEntity.ok(ApiResponseDto.<List<PaymentResponseDto>>builder()
                .status(200)
                .message("Payments retrieved")
                .data(payments)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<PaymentResponseDto>>> getAllPayments() {
        List<PaymentResponseDto> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponseDto.<List<PaymentResponseDto>>builder()
                .status(200)
                .message("All payments retrieved")
                .data(payments)
                .build());
    }

    @PutMapping("/{id}/status")
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
