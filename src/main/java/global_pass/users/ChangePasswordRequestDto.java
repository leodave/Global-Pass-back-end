package global_pass.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Input for changing password — requires old password for verification
@Data
public class ChangePasswordRequestDto {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;
}
