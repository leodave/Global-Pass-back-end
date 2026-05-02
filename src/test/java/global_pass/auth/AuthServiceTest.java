package global_pass.auth;

import global_pass.exception.customUserException.EmailAlreadyExistsException;
import global_pass.exception.customUserException.InvalidPasswordException;
import global_pass.exception.customUserException.UserNotFoundException;
import global_pass.users.User;
import global_pass.users.UserMapper;
import global_pass.users.UserRepository;
import global_pass.users.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private SignupRequestDto signupRequest;
    private LoginRequestDto loginRequest;
    private UserResponseDto userResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("hashedPassword");
        user.setRole(User.Role.USER);
        user.setActive(true);

        signupRequest = new SignupRequestDto();
        signupRequest.setName("John");
        signupRequest.setEmail("john@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        userResponse = UserResponseDto.builder()
                .id(1L)
                .name("John")
                .email("john@example.com")
                .role(User.Role.USER)
                .active(true)
                .build();
    }

    @Test
    void signup_success() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(signupRequest)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(userResponse);
        when(jwtUtil.generateToken("john@example.com", "USER")).thenReturn("mock-jwt-token");

        LoginResponseDto result = authService.signup(signupRequest);

        assertNotNull(result);
        assertEquals("john@example.com", result.getUser().getEmail());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_emailAlreadyExists_throwsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.signup(signupRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(userMapper.toResponseDto(user)).thenReturn(userResponse);
        when(jwtUtil.generateToken("john@example.com", "USER")).thenReturn("mock-jwt-token");

        LoginResponseDto result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("john@example.com", result.getUser().getEmail());
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_invalidPassword_throwsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> authService.login(loginRequest));
    }
}
