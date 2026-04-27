package global_pass.users;

import global_pass.config.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    private Long getAuthenticatedUserId() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getId();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void verifyOwnershipOrAdmin(Long resourceUserId) {
        if (!isAdmin() && !getAuthenticatedUserId().equals(resourceUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUser(@PathVariable Long id) {
        verifyOwnershipOrAdmin(id);
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponseDto.<UserResponseDto>builder()
                .status(200)
                .message("User found")
                .data(user)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequestDto request) {
        verifyOwnershipOrAdmin(id);
        UserResponseDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponseDto.<UserResponseDto>builder()
                .status(200)
                .message("User updated")
                .data(user)
                .build());
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequestDto request) {
        verifyOwnershipOrAdmin(id);
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Password changed")
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteAccount(@PathVariable Long id) {
        verifyOwnershipOrAdmin(id);
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
