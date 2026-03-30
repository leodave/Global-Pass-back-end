package global_pass.global_pass.users;

import org.springframework.stereotype.Component;

// Converts between User entity and DTOs — keeps entity hidden from client
@Component
public class UserMapper {

    // SignupRequestDto → User entity (password will be hashed in service)
    public User toEntity(SignupRequestDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    // User entity → UserResponseDto (password is never included)
    public UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
