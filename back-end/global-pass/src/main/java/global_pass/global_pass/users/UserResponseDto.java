package global_pass.global_pass.users;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
