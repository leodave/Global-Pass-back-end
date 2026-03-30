package global_pass.global_pass.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

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

    // --- Signup Tests ---

    @Test
    void signup_success() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(signupRequest)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(userResponse);

        UserResponseDto result = userService.signup(signupRequest);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_emailAlreadyExists_throwsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.signup(signupRequest));
        verify(userRepository, never()).save(any());
    }

    // --- Login Tests ---

    @Test
    void login_success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(userMapper.toResponseDto(user)).thenReturn(userResponse);

        UserResponseDto result = userService.login(loginRequest);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.login(loginRequest));
    }

    @Test
    void login_invalidPassword_throwsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.login(loginRequest));
    }

    // --- Get User Tests ---

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(userResponse);

        UserResponseDto result = userService.getUserById(1L);

        assertEquals("John", result.getName());
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
    }

    // --- Update User Tests ---

    @Test
    void updateUser_success() {
        UpdateUserRequestDto updateRequest = new UpdateUserRequestDto();
        updateRequest.setName("John Updated");
        updateRequest.setEmail("john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(userResponse);

        UserResponseDto result = userService.updateUser(1L, updateRequest);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_emailTaken_throwsException() {
        UpdateUserRequestDto updateRequest = new UpdateUserRequestDto();
        updateRequest.setName("John");
        updateRequest.setEmail("taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(1L, updateRequest));
    }

    @Test
    void updateUser_notFound_throwsException() {
        UpdateUserRequestDto updateRequest = new UpdateUserRequestDto();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, updateRequest));
    }

    // --- Change Password Tests ---

    @Test
    void changePassword_success() {
        ChangePasswordRequestDto changeRequest = new ChangePasswordRequestDto();
        changeRequest.setOldPassword("password123");
        changeRequest.setNewPassword("newpassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("newHashedPassword");

        userService.changePassword(1L, changeRequest);

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("newpassword123");
    }

    @Test
    void changePassword_wrongOldPassword_throwsException() {
        ChangePasswordRequestDto changeRequest = new ChangePasswordRequestDto();
        changeRequest.setOldPassword("wrongPassword");
        changeRequest.setNewPassword("newpassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.changePassword(1L, changeRequest));
        verify(userRepository, never()).save(any());
    }

    // --- Get All Users Test ---

    @Test
    void getAllUsers_success() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(userResponse);

        List<UserResponseDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("john@example.com", result.get(0).getEmail());
    }
}
