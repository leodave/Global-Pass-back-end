package global_pass.users;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Safe output sent to client — password is never included
@Data
@Builder
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private boolean active;
    private LocalDateTime createdAt;
}
