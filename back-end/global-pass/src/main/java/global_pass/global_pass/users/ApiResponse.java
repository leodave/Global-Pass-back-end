package global_pass.global_pass.users;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
}
