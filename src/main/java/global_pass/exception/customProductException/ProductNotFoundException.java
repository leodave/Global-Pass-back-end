package global_pass.exception.customProductException;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) {
        super("Product not found with id: " + id);
    }
}
