package global_pass.auth;

import global_pass.config.ApiResponseDto;
import global_pass.users.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> signup(@Valid @RequestBody SignupRequestDto request) {
        UserResponseDto user = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.<UserResponseDto>builder()
                .status(201)
                .message("Signup successful")
                .data(user)
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
}
