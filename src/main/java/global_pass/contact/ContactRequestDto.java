package global_pass.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 10, max = 2000, message = "Message must be between 10 and 2000 characters")
    private String message;

    // Honeypot field — should always be empty. Bots fill it, humans don't see it.
    private String website;
}
