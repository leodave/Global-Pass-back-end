package global_pass.payments;

import global_pass.bookings.BookingEntity;
import global_pass.bookings.BookingRepository;
import global_pass.config.SecurityUtil;
import global_pass.exception.customUserException.UserNotFoundException;
import global_pass.users.User;
import global_pass.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final SecurityUtil securityUtil;

    // ✅ Create payment
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        Long userId = securityUtil.getAuthenticatedUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BookingEntity booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // ensure booking belongs to this user
        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Booking does not belong to this user");
        }

        // ensure booking doesn't already have a payment
        if (paymentRepository.existsByBookingId(request.getBookingId())) {
            throw new RuntimeException("A payment already exists for this booking");
        }

        PaymentEntity payment = paymentMapper.toEntity(request, user, booking);
        PaymentEntity saved = paymentRepository.save(payment);
        log.info("Payment created for user: {} booking: {}", userId, request.getBookingId());
        return paymentMapper.toResponseDto(saved);
    }

    // ✅ Get my payments
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getMyPayments() {
        Long userId = securityUtil.getAuthenticatedUserId();
        return paymentRepository.findAllByUserIdWithDetails(userId)
                .stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }

    // ✅ Get single payment
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(String id) {
        PaymentEntity payment = paymentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return paymentMapper.toResponseDto(payment);
    }

    // ✅ Get all payments — admin only
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getAllPayments() {
        return paymentRepository.findAllWithDetails()
                .stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }

    // ✅ Update status — admin only
    @Transactional
    public PaymentResponseDto updateStatus(String id, StatusUpdateRequestDto request) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(request.getStatus());
        if (request.getAdminNote() != null) {
            payment.setAdminNote(request.getAdminNote());
        }

        log.info("Payment {} status updated to {}", id, request.getStatus());
        return paymentMapper.toResponseDto(paymentRepository.save(payment));
    }

    // ✅ Cancel payment
    @Transactional
    public void cancelPayment(String id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.VERIFIED) {
            throw new RuntimeException("Cannot cancel a verified payment");
        }

        paymentRepository.delete(payment);
        log.info("Payment {} cancelled", id);
    }
}
