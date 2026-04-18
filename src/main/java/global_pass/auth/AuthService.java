package global_pass.auth;

import global_pass.exception.customUserException.EmailAlreadyExistsException;
import global_pass.exception.customUserException.InvalidPasswordException;
import global_pass.exception.customUserException.UserNotFoundException;
import global_pass.users.User;
import global_pass.users.UserMapper;
import global_pass.users.UserRepository;
import global_pass.users.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private  final JwtUtil jwtUtil;

    public UserResponseDto signup(SignupRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        log.info("User signed up: {}", saved.getEmail());
        return userMapper.toResponseDto(saved);
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for: {}", request.getEmail());
            throw new InvalidPasswordException();
        }

        // Generate a JWT token using the user's email as the subject
        String token = jwtUtil.generateToken(user.getEmail());

        log.info("User logged in: {}", user.getEmail());
        return LoginResponseDto.builder()
                .token(token)
                .user(userMapper.toResponseDto(user))
                .build();
    }
}
