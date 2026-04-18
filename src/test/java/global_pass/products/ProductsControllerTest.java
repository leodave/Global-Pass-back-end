package global_pass.products;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import global_pass.config.ApiResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {

    @InjectMocks
    private ProductsController productController;

    @Mock
    private IProductService productService;

    private ProductResponseDto responseDto;
    private ProductRequestDto validRequest;

    @BeforeEach
    void setUp() {
        responseDto = ProductResponseDto.builder()
                .id("uuid-123").name("Netflix").description("Streaming")
                .pageLink("https://netflix.com").loginUsername("user@mail.com")
                .loginPassword("secret123").amount(15.99).currency("USD")
                .otherDetails("4K plan").build();

        validRequest = ProductRequestDto.builder()
                .name("Netflix").description("Streaming")
                .pageLink("https://netflix.com").loginUsername("user@mail.com")
                .loginPassword("secret123").amount(15.99).currency("USD")
                .otherDetails("4K plan").build();
    }

    // --- GET /products ---

    @Test
    void getAllProducts_returns_success() {
        when(productService.getAllProducts()).thenReturn(List.of(responseDto));

        ResponseEntity<ApiResponseDto<List<ProductResponseDto>>> actual = productController.getAllProducts();

        assertThat(actual.getStatusCode().is2xxSuccessful()).isTrue();
        assert actual.getBody() != null;
        assertThat(actual.getBody().getStatus()).isEqualTo(200);
        assertThat(actual.getBody().getMessage()).isEqualTo("Products retrieved");
        assertThat(actual.getBody().getData().getFirst().getId()).isEqualTo("uuid-123");
        assertThat(actual.getBody().getData().getFirst().getName()).isEqualTo("Netflix");
    }

    @Test
    void getAllProducts_returns200_withEmptyList() {
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponseDto<List<ProductResponseDto>>> actual = productController.getAllProducts();

        assertThat(actual.getStatusCode().is2xxSuccessful()).isTrue();
        assert actual.getBody() != null;
        assertThat(actual.getBody().getStatus()).isEqualTo(200);
        assertThat(actual.getBody().getData()).isEqualTo(Collections.emptyList());
    }

    // --- GET /products/{id} ---

    @Test
    void getProductById_returns200_withProduct() {
        when(productService.getProductById("uuid-123")).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<ProductResponseDto>> actual = productController.getProductById("uuid-123");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert actual.getBody() != null;
        assertThat(actual.getBody().getStatus()).isEqualTo(200);
        assertThat(actual.getBody().getMessage()).isEqualTo("Product retrieved");
        assertThat(actual.getBody().getData().getId()).isEqualTo("uuid-123");
        assertThat(actual.getBody().getData().getName()).isEqualTo("Netflix");
    }

    @Test
    void getProductById_callsServiceWithCorrectId() {
        when(productService.getProductById("uuid-123")).thenReturn(responseDto);

        productController.getProductById("uuid-123");

        verify(productService, times(1)).getProductById("uuid-123");
    }

    // --- POST /products ---

    @Test
    void createProduct_returns201_withCreatedProduct() {
        when(productService.createProduct(validRequest)).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<ProductResponseDto>> actual = productController.createProduct(validRequest);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assert actual.getBody() != null;
        assertThat(actual.getBody().getStatus()).isEqualTo(201);
        assertThat(actual.getBody().getMessage()).isEqualTo("Product created");
        assertThat(actual.getBody().getData().getId()).isEqualTo("uuid-123");
        assertThat(actual.getBody().getData().getName()).isEqualTo("Netflix");
    }

    @Test
    void createProduct_callsServiceWithCorrectRequest() {
        when(productService.createProduct(validRequest)).thenReturn(responseDto);

        productController.createProduct(validRequest);

        verify(productService, times(1)).createProduct(validRequest);
    }

    // --- PUT /products/{id} ---

    @Test
    void updateProduct_returns200_withUpdatedProduct() {
        ProductRequestDto updateRequest = ProductRequestDto.builder()
                .name("Netflix Premium").description("Streaming HD")
                .pageLink("https://netflix.com").loginUsername("user@mail.com")
                .loginPassword("newSecret").amount(22.99).currency("USD")
                .otherDetails("8K plan").build();

        ProductResponseDto updatedResponse = ProductResponseDto.builder()
                .id("uuid-123").name("Netflix Premium").description("Streaming HD")
                .pageLink("https://netflix.com").loginUsername("user@mail.com")
                .loginPassword("newSecret").amount(22.99).currency("USD")
                .otherDetails("8K plan").build();

        when(productService.updateProduct("uuid-123", updateRequest)).thenReturn(updatedResponse);

        ResponseEntity<ApiResponseDto<ProductResponseDto>> actual = productController.updateProduct("uuid-123", updateRequest);

        assertThat(actual.getStatusCode().is2xxSuccessful()).isTrue();
        assert actual.getBody() != null;
        assertThat(actual.getBody().getStatus()).isEqualTo(200);
        assertThat(actual.getBody().getMessage()).isEqualTo("Product updated");
        assertThat(actual.getBody().getData().getName()).isEqualTo("Netflix Premium");
        assertThat(actual.getBody().getData().getAmount()).isEqualTo(22.99);
    }

    @Test
    void updateProduct_callsServiceWithCorrectIdAndRequest() {
        when(productService.updateProduct("uuid-123", validRequest)).thenReturn(responseDto);

        productController.updateProduct("uuid-123", validRequest);

        verify(productService, times(1)).updateProduct("uuid-123", validRequest);
    }

    // --- DELETE /products/{id} ---

    @Test
    void deleteProduct_returns200_noContent() {
        doNothing().when(productService).deleteProduct("uuid-123");

        ResponseEntity<ApiResponseDto<Void>> actual = productController.deleteProduct("uuid-123");

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert actual.getBody() != null;
        assertThat(actual.getBody().getStatus()).isEqualTo(200);
        assertThat(actual.getBody().getMessage()).isEqualTo("Product deleted");
        assertThat(actual.getBody().getData()).isNull();
    }

    @Test
    void deleteProduct_callsServiceWithCorrectId() {
        doNothing().when(productService).deleteProduct("uuid-123");

        productController.deleteProduct("uuid-123");

        verify(productService, times(1)).deleteProduct("uuid-123");
    }
}
