package global_pass.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// Business logic for user operations
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // Register new user — checks duplicate email, hashes password, saves to DB
    public UserResponseDto signup(SignupRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hash before saving
        User saved = userRepository.save(user);
        log.info("User signed up: {}", saved.getEmail());
        return userMapper.toResponseDto(saved);
    }

    // Authenticate user — finds by email, verifies password
    public UserResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for: {}", request.getEmail());
            throw new InvalidPasswordException();
        }
        log.info("User logged in: {}", user.getEmail());
        return userMapper.toResponseDto(user);
    }

    // Get single user by id
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toResponseDto(user);
    }

    // Update user name and email — checks if new email is already taken
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        // Only check duplicate if email is actually changing
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        User updated = userRepository.save(user);
        log.info("User updated: {}", updated.getEmail());
        return userMapper.toResponseDto(updated);
    }

    // Change password — verifies old password before setting new one
    public void changePassword(Long id, ChangePasswordRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword())); // hash new password
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    // Get all users — admin use
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .toList();
    }
}
