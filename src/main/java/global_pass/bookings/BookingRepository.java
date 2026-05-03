package global_pass.bookings;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository< BookingEntity, String> {

    // ✅ fetch all bookings for user — JOIN FETCH avoids N+1 on user
    @Query("SELECT b FROM BookingEntity b JOIN FETCH b.user WHERE b.user.id = :userId")
    List<BookingEntity> findAllByUserId(@Param("Long") Long userId);

    // ✅ fetch single booking by id and verify ownership in one query
    @Query("SELECT b FROM BookingEntity b JOIN FETCH b.user WHERE b.id = :id AND b.user.id = :userId")
    Optional<BookingEntity> findByIdAndUserId(@Param("id") String id, @Param("userId") Long userId);

    // ✅ fetch by id only — for admin or when ownership verified separately
    @Query("SELECT b FROM BookingEntity b JOIN FETCH b.user WHERE b.id = :id")
    Optional<BookingEntity> findByIdWithUser(@Param("id") String id);

    // ✅ fetch all — admin only
    @Query("SELECT b FROM BookingEntity b JOIN FETCH b.user")
    List<BookingEntity> findAllWithUser();
}
