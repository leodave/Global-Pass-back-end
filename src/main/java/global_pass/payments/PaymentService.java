package global_pass.payments;

import global_pass.exception.customUserException.UserNotFoundException;
import global_pass.exception.customProductException.ProductNotFoundException;
import global_pass.products.ProductEntity;
import global_pass.products.ProductRepository;
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
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    @Transactional
    public PaymentResponseDto uploadPayment(Long userId, String productId, MultipartFile file, String note) {
        validateFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        String storedPath = fileStorageService.store(file, "payments");

        PaymentEntity payment = new PaymentEntity();
        payment.setUserId(userId);
        payment.setProductId(productId);
        payment.setFileName(storedPath);
        payment.setOriginalFileName(file.getOriginalFilename());
        payment.setContentType(file.getContentType());
        payment.setFileSize(file.getSize());
        payment.setNote(note);

        PaymentEntity saved = paymentRepository.save(payment);
        log.info("Payment uploaded: id={}, user={}, product={}", saved.getId(), user.getEmail(), product.getName());
        return toDto(saved, user, product);
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
        log.info("Payment {} status updated to {}", paymentId, status);
        return toDtoWithLookup(saved);
    }

    public Resource getFile(String paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        return fileStorageService.load(payment.getFileName());
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
        ProductEntity product = productRepository.findById(payment.getProductId()).orElse(null);
        return toDto(payment, user, product);
    }

    private PaymentResponseDto toDto(PaymentEntity payment, User user, ProductEntity product) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .userName(user != null ? user.getName() : "Unknown")
                .userEmail(user != null ? user.getEmail() : "Unknown")
                .productId(payment.getProductId())
                .productName(product != null ? product.getName() : "Unknown")
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
