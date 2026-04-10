package global_pass.products;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toResponseDto_returnsNull_whenEntityIsNull() {
        assertThat(mapper.toResponseDto(null)).isNull();
    }

    // --- toEntity ---

    @Test
    void toEntity_mapsAllFields() {
        ProductRequestDto request = buildRequest();
        ProductEntity entity = mapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo(request.getName());
        assertThat(entity.getDescription()).isEqualTo(request.getDescription());
        assertThat(entity.getPageLink()).isEqualTo(request.getPageLink());
        assertThat(entity.getLoginUsername()).isEqualTo(request.getLoginUsername());
        assertThat(entity.getLoginPassword()).isEqualTo(request.getLoginPassword());
        assertThat(entity.getAmount()).isEqualTo(request.getAmount());
        assertThat(entity.getCurrency()).isEqualTo(request.getCurrency());
        assertThat(entity.getOtherDetails()).isEqualTo(request.getOtherDetails());
    }

    @Test
    void toEntity_doesNotMapId() {
        ProductEntity entity = mapper.toEntity(buildRequest());
        assertThat(entity.getId()).isNull(); // ID is DB-generated
    }

    @Test
    void toEntity_returnsNull_whenRequestIsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    // --- updateEntityFromRequest ---

    @Test
    void updateEntityFromRequest_updatesAllFields() {
        ProductEntity existing = buildEntity();
        ProductRequestDto update = ProductRequestDto.builder()
                .name("Disney+")
                .description("Disney streaming")
                .pageLink("https://disneyplus.com")
                .loginUsername("newuser@mail.com")
                .loginPassword("newpass123")
                .amount(8.99)
                .currency("EUR")
                .otherDetails("Basic plan")
                .build();

        mapper.updateEntityFromRequest(update, existing);

        assertThat(existing.getName()).isEqualTo("Disney+");
        assertThat(existing.getDescription()).isEqualTo("Disney streaming");
        assertThat(existing.getPageLink()).isEqualTo("https://disneyplus.com");
        assertThat(existing.getLoginUsername()).isEqualTo("newuser@mail.com");
        assertThat(existing.getLoginPassword()).isEqualTo("newpass123");
        assertThat(existing.getAmount()).isEqualTo(8.99);
        assertThat(existing.getCurrency()).isEqualTo("EUR");
        assertThat(existing.getOtherDetails()).isEqualTo("Basic plan");
    }

    @Test
    void updateEntityFromRequest_preservesId() {
        ProductEntity existing = buildEntity();
        String originalId = existing.getId();
        mapper.updateEntityFromRequest(buildRequest(), existing);
        assertThat(existing.getId()).isEqualTo(originalId);
    }

    // --- toResponseDto ---

    @Test
    void toResponseDto_mapsAllFields() {
        ProductEntity entity = buildEntity();
        ProductResponseDto dto = mapper.toResponseDto(entity);

        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getName()).isEqualTo(entity.getName());
        assertThat(dto.getDescription()).isEqualTo(entity.getDescription());
        assertThat(dto.getPageLink()).isEqualTo(entity.getPageLink());
        assertThat(dto.getLoginUsername()).isEqualTo(entity.getLoginUsername());
        assertThat(dto.getLoginPassword()).isEqualTo(entity.getLoginPassword());
        assertThat(dto.getAmount()).isEqualTo(entity.getAmount());
        assertThat(dto.getCurrency()).isEqualTo(entity.getCurrency());
        assertThat(dto.getOtherDetails()).isEqualTo(entity.getOtherDetails());
    }

    private ProductEntity buildEntity() {
        return new ProductEntity(
                "uuid-123", "Netflix",
                "Streaming service",
                "https://netflix.com",
                "user@mail.com",
                "secret123",
                15.99,
                "USD",
                "4K plan"
        );
    }

    private ProductRequestDto buildRequest() {
        return ProductRequestDto.builder()
                .name("Netflix")
                .description("Streaming service")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .otherDetails("4K plan")
                .build();
    }
}
