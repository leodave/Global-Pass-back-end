package global_pass.bookings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private Long userId;

    private String userName;

    private String userEmail;

    @Column(nullable = false)
    private String serviceId;

    @Column(nullable = false)
    private String serviceName;

    private Double amount;

    private String currency;

    private String status;

    private String paymentId;

    private String paymentNote;

    private String originalFileName;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
