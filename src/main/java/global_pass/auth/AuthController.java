package global_pass.auth;

import global_pass.config.ApiResponseDto;
import global_pass.users.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> signup(@Valid @RequestBody SignupRequestDto request) {
        LoginResponseDto result = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<LoginResponseDto>builder()
                .status(201)
                .message("Signup successful")
                .data(result)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto loginInUser = authService.login(request);
        return ResponseEntity.ok(ApiResponseDto.<LoginResponseDto>builder()
                .status(200)
                .message("Login successful")
                .data(loginInUser)
                .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto<Void>> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        authService.requestPasswordReset(body.get("email"));
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("If an account with that email exists, a reset link has been sent.")
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(@RequestBody java.util.Map<String, String> body) {
        authService.resetPassword(body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Password reset successful")
                .build());
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponseDto<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Email verified successfully")
                .build());
    }
}
