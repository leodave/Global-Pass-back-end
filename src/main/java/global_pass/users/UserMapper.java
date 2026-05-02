package global_pass.users;

import global_pass.auth.SignupRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "resetToken", ignore = true)
    @Mapping(target = "resetTokenExpiry", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    User toEntity(SignupRequestDto dto);

    UserResponseDto toResponseDto(User user);
}
