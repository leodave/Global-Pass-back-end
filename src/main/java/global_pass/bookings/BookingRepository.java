package global_pass.bookings;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository< BookingEntity, String> {

    List<BookingEntity> findAllByUserId(Long userId);
    Optional<BookingEntity> findByIdAndUserId(String id, Long userId);
}
