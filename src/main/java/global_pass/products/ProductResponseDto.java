package global_pass.products;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductResponseDto {
    public String id;
    public String name;
    public String description;
    public String pageLink;
    public String loginUsername;
    public String loginPassword;
    public Double amount;
    public String currency;
    public String otherDetails;
}
