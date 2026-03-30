package global_pass.global_pass.users;

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

    // --- Signup Tests ---

    @Test
    void signup_returns201() {
        when(userService.signup(any(SignupRequestDto.class))).thenReturn(userResponse);

        SignupRequestDto request = new SignupRequestDto();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        ResponseEntity<ApiResponse<UserResponseDto>> response = userController.signup(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(201, response.getBody().getStatus());
        assertEquals("Signup successful", response.getBody().getMessage());
        assertEquals("john@example.com", response.getBody().getData().getEmail());
    }

    @Test
    void signup_duplicateEmail_throwsException() {
        when(userService.signup(any(SignupRequestDto.class)))
                .thenThrow(new EmailAlreadyExistsException("john@example.com"));

        SignupRequestDto request = new SignupRequestDto();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        assertThrows(EmailAlreadyExistsException.class, () -> userController.signup(request));
    }

    // --- Login Tests ---

    @Test
    void login_returns200() {
        when(userService.login(any(LoginRequestDto.class))).thenReturn(userResponse);

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        ResponseEntity<ApiResponse<UserResponseDto>> response = userController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals("John", response.getBody().getData().getName());
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userService.login(any(LoginRequestDto.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        assertThrows(UserNotFoundException.class, () -> userController.login(request));
    }

    @Test
    void login_invalidPassword_throwsException() {
        when(userService.login(any(LoginRequestDto.class)))
                .thenThrow(new InvalidPasswordException());

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("john@example.com");
        request.setPassword("wrongpass");

        assertThrows(InvalidPasswordException.class, () -> userController.login(request));
    }

    // --- Get User Tests ---

    @Test
    void getUser_returns200() {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        ResponseEntity<ApiResponse<UserResponseDto>> response = userController.getUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().getData().getName());
    }

    @Test
    void getUser_notFound_throwsException() {
        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException("User not found with id: 1"));

        assertThrows(UserNotFoundException.class, () -> userController.getUser(1L));
    }

    // --- Update User Tests ---

    @Test
    void updateUser_returns200() {
        when(userService.updateUser(eq(1L), any(UpdateUserRequestDto.class))).thenReturn(userResponse);

        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setName("John Updated");
        request.setEmail("john@example.com");

        ResponseEntity<ApiResponse<UserResponseDto>> response = userController.updateUser(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User updated", response.getBody().getMessage());
    }

    // --- Change Password Tests ---

    @Test
    void changePassword_returns200() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setOldPassword("password123");
        request.setNewPassword("newpassword123");

        ResponseEntity<ApiResponse<Void>> response = userController.changePassword(1L, request);

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

    // --- Get All Users Tests ---

    @Test
    void getAllUsers_returns200() {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        ResponseEntity<ApiResponse<List<UserResponseDto>>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("john@example.com", response.getBody().getData().get(0).getEmail());
    }
}
