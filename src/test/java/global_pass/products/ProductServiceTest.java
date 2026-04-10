package global_pass.products;

import global_pass.Exception.customProductException.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks private ProductService productService;

    private ProductEntity entity;
    private ProductResponseDto responseDto;
    private ProductRequestDto requestDto;

    @BeforeEach
    void setUp() {
        entity = new ProductEntity(
                "uuid-123", "Netflix", "Streaming",
                "https://netflix.com", "user@mail.com", "secret123",
                15.99, "USD", "4K plan"
        );
        responseDto = ProductResponseDto.builder()
                .id("uuid-123").name("Netflix").description("Streaming")
                .pageLink("https://netflix.com").loginUsername("user@mail.com")
                .loginPassword("secret123").amount(15.99).currency("USD")
                .otherDetails("4K plan").build();
        requestDto = ProductRequestDto.builder()
                .name("Netflix").description("Streaming")
                .pageLink("https://netflix.com").loginUsername("user@mail.com")
                .loginPassword("secret123").amount(15.99).currency("USD")
                .otherDetails("4K plan").build();
    }

    // --- getAllProducts ---

    @Test
    void getAllProducts_returnsListOfDtos() {
        when(productRepository.findAll()).thenReturn(List.of(entity));
        when(productMapper.toResponseDto(entity)).thenReturn(responseDto);

        List<ProductResponseDto> result = productService.getAllProducts();

        assertThat(result).hasSize(1).containsExactly(responseDto);
    }

    @Test
    void getAllProducts_returnsEmptyList_whenNoProducts() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductResponseDto> result = productService.getAllProducts();

        assertThat(result).isEmpty();
        verifyNoInteractions(productMapper);
    }

    // --- getProductById ---

    @Test
    void getProductById_returnsDto_whenFound() {
        when(productRepository.findById("uuid-123")).thenReturn(Optional.of(entity));
        when(productMapper.toResponseDto(entity)).thenReturn(responseDto);

        ProductResponseDto result = productService.getProductById("uuid-123");

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    void getProductById_throws_whenNotFound() {
        when(productRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById("bad-id"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("bad-id");
    }

    // --- createProduct ---

    @Test
    void createProduct_savesAndReturnsDto() {
        when(productMapper.toEntity(requestDto)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toResponseDto(entity)).thenReturn(responseDto);

        ProductResponseDto result = productService.createProduct(requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(productRepository).save(entity);
    }

    // --- updateProduct ---

    @Test
    void updateProduct_updatesAndReturnsDto() {
        when(productRepository.findById("uuid-123")).thenReturn(Optional.of(entity));
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toResponseDto(entity)).thenReturn(responseDto);

        ProductResponseDto result = productService.updateProduct("uuid-123", requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(productMapper).updateEntityFromRequest(requestDto, entity);
        verify(productRepository).save(entity);
    }

    @Test
    void updateProduct_throws_whenNotFound() {
        when(productRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct("bad-id", requestDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("bad-id");

        verify(productRepository, never()).save(any());
    }

    // --- deleteProduct ---

    @Test
    void deleteProduct_deletesSuccessfully() {
        when(productRepository.findById("uuid-123")).thenReturn(Optional.of(entity));

        productService.deleteProduct("uuid-123");

        verify(productRepository).deleteById("uuid-123");
    }

    @Test
    void deleteProduct_throws_whenNotFound() {
        when(productRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct("bad-id"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("bad-id");

        verify(productRepository, never()).deleteById(any());
    }
}
