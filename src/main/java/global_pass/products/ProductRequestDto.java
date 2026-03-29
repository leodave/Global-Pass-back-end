package global_pass.products;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRequestDto {

    String name;
    String description;
    String pageLink;
    String loginUsername;
    String loginPassword;
    Double amount;
    String currency;
    String otherDetails;
}
