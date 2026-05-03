package global_pass.bookings;

import global_pass.users.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    private final BookingMapper mapper = Mappers.getMapper(BookingMapper.class);

    // ──────────────────────────────────────────────
    // toEntity
    // ──────────────────────────────────────────────

    @Test
    void toEntity_mapsAllFields() {
        BookingRequestDto request = buildRequest();
        BookingEntity entity = mapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getDescription()).isEqualTo(request.getDescription());
        assertThat(entity.getPageLink()).isEqualTo(request.getPageLink());
        assertThat(entity.getLoginUsername()).isEqualTo(request.getLoginUsername());
        assertThat(entity.getLoginPassword()).isEqualTo(request.getLoginPassword());
        assertThat(entity.getOtherDetails()).isEqualTo(request.getOtherDetails());
    }

    @Test
    void toEntity_doesNotMapId() {
        BookingEntity entity = mapper.toEntity(buildRequest());
        assertThat(entity.getId()).isNull(); // ID is DB-generated
    }

    @Test
    void toEntity_doesNotMapUser() {
        BookingEntity entity = mapper.toEntity(buildRequest());
        assertThat(entity.getUser()).isNull(); // user is set manually in the service
    }

    @Test
    void toEntity_returnsNull_whenRequestIsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    // ──────────────────────────────────────────────
    // updateEntityFromRequest
    // ──────────────────────────────────────────────

    @Test
    void updateEntityFromRequest_updatesAllFields() {
        BookingEntity existing = buildEntity();
        BookingRequestDto update = BookingRequestDto.builder()
                .name("Disney+")
                .description("Disney streaming")
                .pageLink("https://disneyplus.com")
                .loginUsername("newuser@mail.com")
                .loginPassword("newpass123")
                .otherDetails("Basic plan")
                .build();

        mapper.updateEntityFromRequest(update, existing);

        assertThat(existing.getName()).isEqualTo("Disney+");
        assertThat(existing.getDescription()).isEqualTo("Disney streaming");
        assertThat(existing.getPageLink()).isEqualTo("https://disneyplus.com");
        assertThat(existing.getLoginUsername()).isEqualTo("newuser@mail.com");
        assertThat(existing.getLoginPassword()).isEqualTo("newpass123");
        assertThat(existing.getOtherDetails()).isEqualTo("Basic plan");
    }

    @Test
    void updateEntityFromRequest_preservesId() {
        BookingEntity existing = buildEntity();
        String originalId = existing.getId();
        mapper.updateEntityFromRequest(buildRequest(), existing);
        assertThat(existing.getId()).isEqualTo(originalId);
    }

    @Test
    void updateEntityFromRequest_preservesUser() {
        BookingEntity existing = buildEntity();
        User originalUser = existing.getUser();
        mapper.updateEntityFromRequest(buildRequest(), existing);
        assertThat(existing.getUser()).isEqualTo(originalUser); // user must not be overwritten
    }

    // ──────────────────────────────────────────────
    // toResponseDto
    // ──────────────────────────────────────────────

    @Test
    void toResponseDto_mapsAllFields() {
        BookingEntity entity = buildEntity();
        BookingResponseDto dto = mapper.toResponseDto(entity);

        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getUserId()).isEqualTo(entity.getUser().getId()); // user.id → userId
        assertThat(dto.getName()).isEqualTo(entity.getName());
        assertThat(dto.getDescription()).isEqualTo(entity.getDescription());
        assertThat(dto.getPageLink()).isEqualTo(entity.getPageLink());
        assertThat(dto.getLoginUsername()).isEqualTo(entity.getLoginUsername());
        assertThat(dto.getLoginPassword()).isEqualTo(entity.getLoginPassword());
        assertThat(dto.getOtherDetails()).isEqualTo(entity.getOtherDetails());
    }

    @Test
    void toResponseDto_returnsNull_whenEntityIsNull() {
        assertThat(mapper.toResponseDto(null)).isNull();
    }

    @Test
    void toResponseDto_mapsUserIdFromNestedUser() {
        BookingEntity entity = buildEntity();
        BookingResponseDto dto = mapper.toResponseDto(entity);
        assertThat(dto.getUserId()).isEqualTo(1L); // verifies user.id → userId mapping
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private BookingEntity buildEntity() {
        User user = new User();
        user.setId(1L);

        BookingEntity entity = new BookingEntity();
        entity.setId("uuid-123");
        entity.setUser(user);
        entity.setName("Netflix");
        entity.setDescription("Streaming service");
        entity.setPageLink("https://netflix.com");
        entity.setLoginUsername("user@mail.com");
        entity.setLoginPassword("secret123");
        entity.setOtherDetails("4K plan");
        return entity;
    }

    private BookingRequestDto buildRequest() {
        return BookingRequestDto.builder()
                .name("Netflix")
                .description("Streaming service")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .otherDetails("4K plan")
                .build();
    }
}
