package global_pass.users;

import global_pass.exception.customUserException.EmailAlreadyExistsException;
import global_pass.exception.customUserException.InvalidPasswordException;
import global_pass.exception.customUserException.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        User updated = userRepository.save(user);

        // ✅ no need to update payments — they fetch user data live via JOIN FETCH
        log.info("User updated: {}", updated.getEmail());
        return userMapper.toResponseDto(updated);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void deleteAccount(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // cascade delete handles payments and bookings
        // if cascade is not set on DB level, payments are deleted via orphanRemoval
        userRepository.delete(user);
        log.info("Account deleted: {}", user.getEmail());
    }
}
