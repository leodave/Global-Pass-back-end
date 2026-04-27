package global_pass.users;

import global_pass.bookings.BookingRepository;
import global_pass.exception.customUserException.EmailAlreadyExistsException;
import global_pass.exception.customUserException.InvalidPasswordException;
import global_pass.exception.customUserException.UserNotFoundException;
import global_pass.payments.PaymentRepository;
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
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

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

        paymentRepository.findByUserIdOrderByCreatedAtDesc(id).forEach(p -> {
            p.setUserName(updated.getName());
            p.setUserEmail(updated.getEmail());
            paymentRepository.save(p);
        });
        bookingRepository.findAllByUserIdOrderByCreatedAtDesc(id).forEach(b -> {
            b.setUserName(updated.getName());
            b.setUserEmail(updated.getEmail());
            bookingRepository.save(b);
        });

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

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void deleteAccount(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        paymentRepository.findByUserIdOrderByCreatedAtDesc(id).forEach(paymentRepository::delete);
        bookingRepository.findAllByUserIdOrderByCreatedAtDesc(id).forEach(bookingRepository::delete);
        userRepository.delete(user);
        log.info("Account deleted: {}", user.getEmail());
    }
}
