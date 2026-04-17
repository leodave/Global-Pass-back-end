package global_pass.auth;

import global_pass.exception.EmailAlreadyExistsException;
import global_pass.exception.InvalidPasswordException;
import global_pass.exception.UserNotFoundException;
import global_pass.users.User;
import global_pass.users.UserApiResponseDto;
import global_pass.users.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

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
    void signup_returns201() {
        when(authService.signup(any(SignupRequestDto.class))).thenReturn(userResponse);

        SignupRequestDto request = new SignupRequestDto();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        ResponseEntity<UserApiResponseDto<UserResponseDto>> response = authController.signup(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(201, response.getBody().getStatus());
        assertEquals("Signup successful", response.getBody().getMessage());
        assertEquals("john@example.com", response.getBody().getData().getEmail());
    }

    @Test
    void signup_duplicateEmail_throwsException() {
        when(authService.signup(any(SignupRequestDto.class)))
                .thenThrow(new EmailAlreadyExistsException("john@example.com"));

        SignupRequestDto request = new SignupRequestDto();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        assertThrows(EmailAlreadyExistsException.class, () -> authController.signup(request));
    }

    @Test
    void login_returns200() {
        when(authService.login(any(LoginRequestDto.class))).thenReturn(userResponse);

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        ResponseEntity<UserApiResponseDto<UserResponseDto>> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals("John", response.getBody().getData().getName());
    }

    @Test
    void login_userNotFound_throwsException() {
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        assertThrows(UserNotFoundException.class, () -> authController.login(request));
    }

    @Test
    void login_invalidPassword_throwsException() {
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new InvalidPasswordException());

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("john@example.com");
        request.setPassword("wrongpass");

        assertThrows(InvalidPasswordException.class, () -> authController.login(request));
    }
}
