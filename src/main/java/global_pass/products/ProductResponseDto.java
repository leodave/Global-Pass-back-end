package global_pass.products;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductResponseDto {
    private String id;
    private String name;
    private String description;
    private String pageLink;
    private String loginUsername;
    private String loginPassword;
    private Double amount;
    private String currency;
    private String otherDetails;
}
