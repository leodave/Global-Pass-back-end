package global_pass.global_pass.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Input for updating user profile (name and email only)
@Data
public class UpdateUserRequestDto {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;
}
