package global_pass.users;

import lombok.Builder;
import lombok.Data;

// Standard API response wrapper — all endpoints return this format
// Example: { "status": 200, "message": "Login successful", "data": {...} }
@Data
@Builder
public class UserApiResponseDto<T> {

    private int status;
    private String message;
    private T data;
}
