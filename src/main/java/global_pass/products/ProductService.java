package global_pass.products;

import java.util.List;

import global_pass.exception.customProductException.ProductNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponseDto> getAllProducts() {
        log.info("Fetching all products");
        List<ProductResponseDto> products = productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
        log.info("Found {} products", products.size());
        return products;
    }

    @Override
    public ProductResponseDto getProductById(String id) {
        log.info("Fetching product with id: {}", id);
        ProductEntity product = findProductOrThrow(id);
        return productMapper.toResponseDto(product);
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto request) {
        log.info("Creating product with name: {}", request.getName());
        ProductEntity entity = productMapper.toEntity(request);
        ProductEntity saved = productRepository.save(entity);
        log.info("Product created successfully with id: {}", saved.getId());
        return productMapper.toResponseDto(saved);
    }

    @Override
    public ProductResponseDto updateProduct(String id, ProductRequestDto request) {
        log.info("Updating product with id: {}", id);
        ProductEntity existing = findProductOrThrow(id);
        productMapper.updateEntityFromRequest(request, existing);
        ProductEntity saved = productRepository.save(existing);
        log.info("Product updated successfully with id: {}", saved.getId());
        return productMapper.toResponseDto(saved);
    }

    @Override
    public void deleteProduct(String id) {
        log.info("Deleting product with id: {}", id);
        findProductOrThrow(id);
        log.info("Product deleted successfully with id: {}", id);
        productRepository.deleteById(id);
    }

    private ProductEntity findProductOrThrow(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ProductNotFoundException(id);
                });
    }
}
