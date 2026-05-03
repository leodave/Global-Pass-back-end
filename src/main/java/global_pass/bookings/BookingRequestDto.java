package global_pass.bookings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Page link is required")
    @Pattern(
            regexp = "^(https?://).*",
            message = "Page link must be a valid URL starting with http:// or https://"
    )
    private String pageLink;

    @NotBlank(message = "Login username is required")
    private String loginUsername;

    @NotBlank(message = "Login password is required")
    private String loginPassword;

    @Size(max = 1000, message = "Other details cannot exceed 1000 characters")
    private String otherDetails;
}
