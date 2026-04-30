package global_pass.users;

import global_pass.config.ApiResponseDto;
import global_pass.config.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUser(@PathVariable Long id) {
        securityUtil.verifyOwnershipOrAdmin(id);
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponseDto.<UserResponseDto>builder()
                .status(200)
                .message("User found")
                .data(user)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequestDto request) {
        securityUtil.verifyOwnershipOrAdmin(id);
        UserResponseDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponseDto.<UserResponseDto>builder()
                .status(200)
                .message("User updated")
                .data(user)
                .build());
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequestDto request) {
        securityUtil.verifyOwnershipOrAdmin(id);
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Password changed")
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteAccount(@PathVariable Long id) {
        securityUtil.verifyOwnershipOrAdmin(id);
        userService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Account deleted")
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponseDto.<List<UserResponseDto>>builder()
                .status(200)
                .message("Users retrieved")
                .data(users)
                .build());
    }
}
