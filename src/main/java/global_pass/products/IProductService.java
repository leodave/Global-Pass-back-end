package global_pass.products;

import java.util.List;

public interface IProductService {
    List<ProductResponseDto> getAllProducts();
    ProductResponseDto getProductById(String id);
    ProductResponseDto createProduct(ProductRequestDto request);
    ProductResponseDto updateProduct(String id, ProductRequestDto request);
    void deleteProduct(String id);
}
