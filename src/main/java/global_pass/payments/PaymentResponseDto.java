package global_pass.payments;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponseDto {
    private String id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String productId;
    private String productName;
    private String originalFileName;
    private String contentType;
    private long fileSize;
    private String note;
    private PaymentStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
}
