package global_pass.products;

import java.util.List;

import global_pass.config.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("public/api/v1/products")
public class ProductsController {

    private IProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ProductResponseDto>>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponseDto.<List<ProductResponseDto>>builder()
                .status(200)
                .message("Products retrieved")
                .data(products)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> getProductById(@PathVariable String id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponseDto.<ProductResponseDto>builder()
                .status(200)
                .message("Product retrieved")
                .data(product)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> createProduct(
            @Valid @RequestBody ProductRequestDto request) {
        ProductResponseDto product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.<ProductResponseDto>builder()
                        .status(201)
                        .message("Product created")
                        .data(product)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequestDto request) {
        ProductResponseDto product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponseDto.<ProductResponseDto>builder()
                .status(200)
                .message("Product updated")
                .data(product)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Product deleted")
                .data(null)
                .build());
    }
}
