package global_pass.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {

    // Fetch all payments for a user — JOIN FETCH avoids N+1
    @Query("SELECT p FROM PaymentEntity p JOIN FETCH p.user JOIN FETCH p.booking WHERE p.user.id = :userId")
    List<PaymentEntity> findAllByUserIdWithDetails(@Param("userId") Long userId);

    // Fetch single payment with all relations
    @Query("SELECT p FROM PaymentEntity p JOIN FETCH p.user JOIN FETCH p.booking WHERE p.id = :id")
    Optional<PaymentEntity> findByIdWithDetails(@Param("id") String id);

    // Fetch payment by booking
    @Query("SELECT p FROM PaymentEntity p JOIN FETCH p.user JOIN FETCH p.booking WHERE p.booking.id = :bookingId")
    Optional<PaymentEntity> findByBookingIdWithDetails(@Param("bookingId") String bookingId);

    @Query("SELECT p FROM PaymentEntity p JOIN FETCH p.user JOIN FETCH p.booking")
    List<PaymentEntity> findAllWithDetails();

    boolean existsByBookingId(String bookingId);
}
