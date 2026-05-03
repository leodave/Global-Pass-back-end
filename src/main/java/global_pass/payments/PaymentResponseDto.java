package global_pass.payments;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponseDto {
    // payment fields
    private String id;
    private Double amount;
    private String currency;
    private String imageUrl;
    private PaymentStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // booking reference
    private String bookingId;

    // user info — name and email only, no sensitive data
    private Long userId;
    private String userName;
    private String userEmail;
}
