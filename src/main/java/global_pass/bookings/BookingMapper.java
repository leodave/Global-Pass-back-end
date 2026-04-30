package global_pass.bookings;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    // Entity → ResponseDto
    @Mapping(source = "user.id", target = "userId")
    BookingResponseDto toResponseDto(BookingEntity entity);

    // RequestDto → Entity (for create) — user is set manually in the service
    @Mapping(target = "user", ignore = true)
    BookingEntity toEntity(BookingRequestDto dto);

    // RequestDto → existing Entity (for update)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(BookingRequestDto dto, @MappingTarget BookingEntity entity);

}
