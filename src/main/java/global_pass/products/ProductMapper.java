package global_pass.products;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Entity → ResponseDto
    ProductResponseDto toResponseDto(ProductEntity entity);

    // RequestDto → Entity (for create)
    ProductEntity toEntity(ProductRequestDto dto);

    // RequestDto → existing Entity (for update, avoids creating a new object)
    void updateEntityFromRequest(ProductRequestDto dto, @MappingTarget ProductEntity entity);

}
