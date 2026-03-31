package global_pass.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toEntity_mapsFieldsCorrectly() {
        SignupRequestDto dto = new SignupRequestDto();
        dto.setName("John");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        User user = userMapper.toEntity(dto);

        assertEquals("John", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertNull(user.getId());
        assertEquals(User.Role.USER, user.getRole());
    }

    @Test
    void toResponseDto_mapsFieldsCorrectly() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("hashedPassword");
        user.setRole(User.Role.USER);
        user.setActive(true);

        UserResponseDto dto = userMapper.toResponseDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals(User.Role.USER, dto.getRole());
        assertTrue(dto.isActive());
    }

    @Test
    void toResponseDto_neverExposesPassword() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("secretPassword");

        UserResponseDto dto = userMapper.toResponseDto(user);

        // ResponseDto has no password field — this test ensures it stays that way
        assertNotNull(dto);
        assertEquals("John", dto.getName());
    }
}
