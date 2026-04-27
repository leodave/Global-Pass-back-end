package global_pass.bookings;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, String> {

    List<BookingEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<BookingEntity> findAllByOrderByCreatedAtDesc();

    java.util.Optional<BookingEntity> findByPaymentId(String paymentId);
}
