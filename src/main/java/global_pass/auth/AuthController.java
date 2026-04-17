package global_pass.auth;

import global_pass.users.UserApiResponseDto;
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
    public ResponseEntity<UserApiResponseDto<UserResponseDto>> signup(@Valid @RequestBody SignupRequestDto request) {
        UserResponseDto user = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserApiResponseDto.<UserResponseDto>builder()
                .status(201)
                .message("Signup successful")
                .data(user)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<UserApiResponseDto<UserResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        UserResponseDto user = authService.login(request);
        return ResponseEntity.ok(UserApiResponseDto.<UserResponseDto>builder()
                .status(200)
                .message("Login successful")
                .data(user)
                .build());
    }
}
