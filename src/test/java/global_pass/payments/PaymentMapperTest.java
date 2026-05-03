package global_pass.payments;

import global_pass.bookings.BookingEntity;
import global_pass.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class PaymentMapperTest {

    private final PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);


    @Mock
    private User user;
    @Mock
    private BookingEntity booking;
    @Mock
    private PaymentRequestDto request;
    @Mock
    private PaymentEntity entity;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setName("Dave");
        user.setEmail("dave@mail.com");

        booking = new BookingEntity();
        booking.setId("booking-1");

        request = new PaymentRequestDto();
        request.setBookingId("booking-1");
        request.setAmount(100.0);
        request.setCurrency("USD");
        request.setImageUrl("http://image.url");
        request.setAdminNote("note");

        entity = new PaymentEntity();
        entity.setId("payment-1");
        entity.setUser(user);
        entity.setBooking(booking);
        entity.setAmount(100.0);
        entity.setCurrency("USD");
        entity.setImageUrl("http://image.url");
        entity.setStatus(PaymentStatus.PENDING);
        entity.setAdminNote("note");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void toEntity_shouldMapAllFields() {
        PaymentEntity result = mapper.toEntity(request, user, booking);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getBooking()).isEqualTo(booking);
        assertThat(result.getAmount()).isEqualTo(100.0);
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getImageUrl()).isEqualTo("http://image.url");
        assertThat(result.getAdminNote()).isEqualTo("note");
        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void toEntity_shouldDefaultStatusToPending() {
        PaymentEntity result = mapper.toEntity(request, user, booking);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void toResponseDto_shouldFlattenUserAndBooking() {
        PaymentResponseDto result = mapper.toResponseDto(entity);

        assertThat(result.getId()).isEqualTo("payment-1");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUserName()).isEqualTo("Dave");
        assertThat(result.getUserEmail()).isEqualTo("dave@mail.com");
        assertThat(result.getBookingId()).isEqualTo("booking-1");
        assertThat(result.getAmount()).isEqualTo(100.0);
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getAdminNote()).isEqualTo("note");
    }
}
