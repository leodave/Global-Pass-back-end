package global_pass.payments;

import global_pass.bookings.BookingEntity;
import global_pass.bookings.BookingRepository;
import global_pass.config.ServiceCatalog;
import global_pass.exception.customUserException.UserNotFoundException;
import global_pass.users.User;
import global_pass.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
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
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final FileStorageService fileStorageService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    @Transactional
    public PaymentResponseDto uploadPayment(Long userId, String bookingId, MultipartFile file, Double amount, String note) {
        validateFile(file);

        if (amount != null && amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (paymentRepository.existsByUserIdAndBookingIdAndStatus(userId, bookingId, PaymentStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending payment for this service. Cancel it first to submit a new one.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        if (!ServiceCatalog.exists(bookingId)) {
            throw new IllegalArgumentException("Unknown service: " + bookingId);
        }

        String safeName = user.getName().replaceAll("[^a-zA-Z0-9]", "_");
        String safeService = ServiceCatalog.getNameById(bookingId).replaceAll("[^a-zA-Z0-9]", "_");
        String ext = getExtension(file.getOriginalFilename());
        String storedFileName = safeName + "_" + safeService + "_" + System.currentTimeMillis() + ext;
        String storedPath = fileStorageService.store(file, "payments", storedFileName);

        PaymentEntity payment = new PaymentEntity();
        payment.setUserId(userId);
        payment.setUserName(user.getName());
        payment.setUserEmail(user.getEmail());
        payment.setBookingId(bookingId);
        payment.setBookingName(ServiceCatalog.getNameById(bookingId));
        payment.setAmount(amount);
        payment.setFileName(storedPath);
        payment.setOriginalFileName(file.getOriginalFilename());
        payment.setContentType(file.getContentType());
        payment.setFileSize(file.getSize());
        payment.setNote(note);

        PaymentEntity saved = paymentRepository.save(payment);

        BookingEntity booking = new BookingEntity();
        booking.setUserId(userId);
        booking.setUserName(user.getName());
        booking.setUserEmail(user.getEmail());
        booking.setServiceId(bookingId);
        booking.setServiceName(ServiceCatalog.getNameById(bookingId));
        booking.setAmount(amount);
        booking.setCurrency("EUR");
        booking.setStatus("PENDING");
        booking.setPaymentId(saved.getId());
        booking.setPaymentNote(note);
        booking.setOriginalFileName(file.getOriginalFilename());
        bookingRepository.save(booking);

        log.info("Payment uploaded: id={}, user={}, booking={}", saved.getId(), user.getEmail(), ServiceCatalog.getNameById(bookingId));
        return toDto(saved, user);
    }

    public List<PaymentResponseDto> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDtoWithLookup)
                .toList();
    }

    public List<PaymentResponseDto> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDtoWithLookup)
                .toList();
    }

    @Transactional
    public PaymentResponseDto updateStatus(String paymentId, PaymentStatus status, String adminNote) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        payment.setStatus(status);
        payment.setAdminNote(adminNote);
        PaymentEntity saved = paymentRepository.save(payment);

        bookingRepository.findByPaymentId(paymentId)
                .ifPresent(b -> {
                    b.setStatus(status.name());
                    bookingRepository.save(b);
                });

        log.info("Payment {} status updated to {}", paymentId, status);
        return toDtoWithLookup(saved);
    }

    public Resource getFile(String paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        return fileStorageService.load(payment.getFileName());
    }

    @Transactional
    public void cancelPayment(String paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be cancelled");
        }
        try {
            fileStorageService.delete(payment.getFileName());
        } catch (Exception e) {
            log.warn("Failed to delete file for payment {}: {}", paymentId, e.getMessage());
        }
        paymentRepository.delete(payment);
        bookingRepository.findByPaymentId(paymentId).ifPresent(bookingRepository::delete);
        log.info("Payment cancelled: id={}", paymentId);
    }

    @Transactional
    public PaymentResponseDto reuploadFile(String paymentId, MultipartFile file) {
        validateFile(file);
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can have their file replaced");
        }
        User user = userRepository.findById(payment.getUserId()).orElse(null);
        String oldFileName = payment.getFileName();
        String safeName = user != null ? user.getName().replaceAll("[^a-zA-Z0-9]", "_") : "unknown";
        String safeService = ServiceCatalog.getNameById(payment.getBookingId()).replaceAll("[^a-zA-Z0-9]", "_");
        String ext = getExtension(file.getOriginalFilename());
        String storedFileName = safeName + "_" + safeService + "_" + System.currentTimeMillis() + ext;
        String storedPath = fileStorageService.store(file, "payments", storedFileName);

        payment.setFileName(storedPath);
        payment.setOriginalFileName(file.getOriginalFilename());
        payment.setContentType(file.getContentType());
        payment.setFileSize(file.getSize());
        try {
            fileStorageService.delete(oldFileName);
        } catch (Exception e) {
            log.warn("Failed to delete old file {}: {}", oldFileName, e.getMessage());
        }
        PaymentEntity saved = paymentRepository.save(payment);
        log.info("Payment file replaced: id={}", paymentId);
        return toDto(saved, user);
    }

    public PaymentEntity getPaymentEntity(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 10MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not supported. Allowed: PNG, JPG, WEBP, PDF, DOC, DOCX, XLS, XLSX");
        }
    }

    private PaymentResponseDto toDtoWithLookup(PaymentEntity payment) {
        User user = userRepository.findById(payment.getUserId()).orElse(null);
        return toDto(payment, user);
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

    private PaymentResponseDto toDto(PaymentEntity payment, User user) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .userName(user != null ? user.getName() : "Unknown")
                .userEmail(user != null ? user.getEmail() : "Unknown")
                .bookingId(payment.getBookingId())
                .bookingName(payment.getBookingName() != null ? payment.getBookingName() : ServiceCatalog.getNameById(payment.getBookingId()))
                .amount(payment.getAmount())
                .originalFileName(payment.getOriginalFileName())
                .contentType(payment.getContentType())
                .fileSize(payment.getFileSize())
                .note(payment.getNote())
                .status(payment.getStatus())
                .adminNote(payment.getAdminNote())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
