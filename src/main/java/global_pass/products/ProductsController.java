package global_pass.products;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("public/api/v1/products")
public class ProductsController {

    private ProductService productService;

    @GetMapping()
    public List<ProductEntity> getAllProducts() {
        return productService.getAllProducts();
    }
}
