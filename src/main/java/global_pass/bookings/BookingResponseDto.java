package global_pass.bookings;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BookingResponseDto {
    private String id;
    private Long userId;
    private String name;
    private String description;
    private String pageLink;
    private String loginUsername;
    private String loginPassword;
    private String otherDetails;
}
