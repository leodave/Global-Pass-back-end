package global_pass.auth;

import global_pass.users.UserResponseDto;
import lombok.Builder;
import lombok.Data;

// DTO returned after a successful login containing the token and user info
@Data
@Builder
public class LoginResponseDto {

    // JWT token the frontend will use for subsequent requests
    private String token;

    // User details returned after login
    private UserResponseDto user;
}
