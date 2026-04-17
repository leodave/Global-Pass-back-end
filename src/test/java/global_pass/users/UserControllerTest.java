package global_pass.users;

import global_pass.config.ApiResponseDto;
import global_pass.exception.customUserException.InvalidPasswordException;
import global_pass.exception.customUserException.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserResponseDto userResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponseDto.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .role(User.Role.USER)
                .active(true)
                .build();
    }

    @Test
    void getUser_returns200() {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        ResponseEntity<ApiResponseDto<UserResponseDto>> response = userController.getUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().getData().getName());
    }

    @Test
    void getUser_notFound_throwsException() {
        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException("User not found with id: 1"));

        assertThrows(UserNotFoundException.class, () -> userController.getUser(1L));
    }

    @Test
    void updateUser_returns200() {
        when(userService.updateUser(eq(1L), any(UpdateUserRequestDto.class))).thenReturn(userResponse);

        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setName("John Updated");
        request.setEmail("john@example.com");

        ResponseEntity<ApiResponseDto<UserResponseDto>> response = userController.updateUser(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User updated", response.getBody().getMessage());
    }

    @Test
    void changePassword_returns200() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setOldPassword("password123");
        request.setNewPassword("newpassword123");

        ResponseEntity<ApiResponseDto<Void>> response = userController.changePassword(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed", response.getBody().getMessage());
    }

    @Test
    void changePassword_wrongOldPassword_throwsException() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newpassword123");

        doThrow(new InvalidPasswordException()).when(userService).changePassword(eq(1L), any(ChangePasswordRequestDto.class));

        assertThrows(InvalidPasswordException.class, () -> userController.changePassword(1L, request));
    }

    @Test
    void getAllUsers_returns200() {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        ResponseEntity<ApiResponseDto<List<UserResponseDto>>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("john@example.com", response.getBody().getData().get(0).getEmail());
    }
}
