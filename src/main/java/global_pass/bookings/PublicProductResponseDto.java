package global_pass.bookings;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicProductResponseDto {
    private String id;
    private String name;
    private String description;
    private Double amount;
    private String currency;
    private String otherDetails;
}
