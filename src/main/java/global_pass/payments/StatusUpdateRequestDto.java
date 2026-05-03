package global_pass.payments;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequestDto {

    @NotNull(message = "Status is required")
    private PaymentStatus status;

    private String adminNote;
}
