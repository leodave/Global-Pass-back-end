package global_pass.payments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {

    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PaymentEntity> findAllByOrderByCreatedAtDesc();
}
