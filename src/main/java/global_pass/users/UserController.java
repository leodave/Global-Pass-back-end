package global_pass.global_pass.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Handles all user-related HTTP requests
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- Public auth endpoints ---

    // POST /api/auth/signup — register a new user
    @PostMapping("/api/auth/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(@Valid @RequestBody SignupRequestDto request) {
        UserResponseDto user = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<UserResponseDto>builder()
                .status(201)
                .message("Signup successful")
                .data(user)
                .build());
    }

    // POST /api/auth/login — authenticate user with email & password
    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<UserResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        UserResponseDto user = userService.login(request);
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .status(200)
                .message("Login successful")
                .data(user)
                .build());
    }

    // --- Protected user endpoints (will require JWT later) ---

    // GET /api/users/{id} — get user profile by id
    @GetMapping("/api/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .status(200)
                .message("User found")
                .data(user)
                .build());
    }

    // PUT /api/users/{id} — update user name and email
    @PutMapping("/api/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequestDto request) {
        UserResponseDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.<UserResponseDto>builder()
                .status(200)
                .message("User updated")
                .data(user)
                .build());
    }

    // PUT /api/users/{id}/password — change password (requires old password)
    @PutMapping("/api/users/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequestDto request) {
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Password changed")
                .build());
    }

    // --- Admin endpoints (will require admin role later) ---

    // GET /api/users — get all users (admin only)
    @GetMapping("/api/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.<List<UserResponseDto>>builder()
                .status(200)
                .message("Users retrieved")
                .data(users)
                .build());
    }
}
