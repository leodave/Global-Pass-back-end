package global_pass.payments;

import global_pass.bookings.BookingEntity;
import global_pass.users.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", source = "user")       // ← explicit
    @Mapping(target = "booking", source = "booking") // ← explicit
    @Mapping(target = "amount", source = "dto.amount")
    @Mapping(target = "currency", source = "dto.currency")
    @Mapping(target = "imageUrl", source = "dto.imageUrl")
    @Mapping(target = "adminNote", source = "dto.adminNote")
    PaymentEntity toEntity(PaymentRequestDto dto, User user, BookingEntity booking);

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userEmail", source = "user.email")
    PaymentResponseDto toResponseDto(PaymentEntity entity);
}
