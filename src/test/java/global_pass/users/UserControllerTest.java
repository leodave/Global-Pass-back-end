package global_pass.users;

import global_pass.config.ApiResponseDto;
import global_pass.config.SecurityUtil;
import global_pass.exception.customUserException.InvalidPasswordException;
import global_pass.exception.customUserException.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private UserController userController;

    private static final Long USER_ID = 1L;

    private UserResponseDto userResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponseDto.builder()
                .id(USER_ID)
                .name("John")
                .email("john@example.com")
                .role(User.Role.USER)
                .active(true)
                .build();
    }

    // ──────────────────────────────────────────────
    // GET /users/{id}
    // ──────────────────────────────────────────────

    @Test
    void getUser_shouldReturn200() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        when(userService.getUserById(USER_ID)).thenReturn(userResponse);

        ResponseEntity<ApiResponseDto<UserResponseDto>> response = userController.getUser(USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getName()).isEqualTo("John");
        assertThat(response.getBody().getData().getEmail()).isEqualTo("john@example.com");
        verify(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        verify(userService).getUserById(USER_ID);
    }

    @Test
    void getUser_shouldThrow_whenNotFound() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        when(userService.getUserById(USER_ID))
                .thenThrow(new UserNotFoundException("User not found with id: 1"));

        assertThatThrownBy(() -> userController.getUser(USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getUser_shouldThrow_whenAccessDenied() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        assertThatThrownBy(() -> userController.getUser(USER_ID))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(userService, never()).getUserById(any());
    }

    // ──────────────────────────────────────────────
    // PUT /users/{id}
    // ──────────────────────────────────────────────

    @Test
    void updateUser_shouldReturn200() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        when(userService.updateUser(eq(USER_ID), any(UpdateUserRequestDto.class)))
                .thenReturn(userResponse);

        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setName("John Updated");
        request.setEmail("john@example.com");

        ResponseEntity<ApiResponseDto<UserResponseDto>> response =
                userController.updateUser(USER_ID, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("User updated");
        assertThat(response.getBody().getData().getName()).isEqualTo("John");
        verify(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        verify(userService).updateUser(eq(USER_ID), any());
    }

    @Test
    void updateUser_shouldThrow_whenAccessDenied() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setName("John");
        request.setEmail("john@example.com");

        assertThatThrownBy(() -> userController.updateUser(USER_ID, request))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(userService, never()).updateUser(any(), any());
    }

    // ──────────────────────────────────────────────
    // PUT /users/{id}/password
    // ──────────────────────────────────────────────

    @Test
    void changePassword_shouldReturn200() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setOldPassword("password123");
        request.setNewPassword("newpassword123");

        ResponseEntity<ApiResponseDto<Void>> response =
                userController.changePassword(USER_ID, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Password changed");
        verify(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        verify(userService).changePassword(eq(USER_ID), any());
    }

    @Test
    void changePassword_shouldThrow_whenWrongOldPassword() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        doThrow(new InvalidPasswordException())
                .when(userService).changePassword(eq(USER_ID), any());

        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newpassword123");

        assertThatThrownBy(() -> userController.changePassword(USER_ID, request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void changePassword_shouldThrow_whenAccessDenied() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        ChangePasswordRequestDto request = new ChangePasswordRequestDto();
        request.setOldPassword("password123");
        request.setNewPassword("newpassword123");

        assertThatThrownBy(() -> userController.changePassword(USER_ID, request))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(userService, never()).changePassword(any(), any());
    }

    // ──────────────────────────────────────────────
    // GET /users (admin)
    // ──────────────────────────────────────────────

    @Test
    void getAllUsers_shouldReturn200() {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        ResponseEntity<ApiResponseDto<List<UserResponseDto>>> response =
                userController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getEmail()).isEqualTo("john@example.com");
        verify(userService).getAllUsers();
        verifyNoInteractions(securityUtil); // admin endpoint — no ownership check
    }

    @Test
    void getAllUsers_shouldReturnEmptyList() {
        when(userService.getAllUsers()).thenReturn(List.of());

        ResponseEntity<ApiResponseDto<List<UserResponseDto>>> response =
                userController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEmpty();
    }

    // ──────────────────────────────────────────────
    // DELETE /users/{id}
    // ──────────────────────────────────────────────

    @Test
    void deleteAccount_shouldReturn200() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        doNothing().when(userService).deleteAccount(USER_ID);

        ResponseEntity<ApiResponseDto<Void>> response =
                userController.deleteAccount(USER_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Account deleted");
        verify(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        verify(userService).deleteAccount(USER_ID);
    }

    @Test
    void deleteAccount_shouldThrow_whenNotFound() {
        doNothing().when(securityUtil).verifyOwnershipOrAdmin(USER_ID);
        doThrow(new UserNotFoundException("User not found with id: 1"))
                .when(userService).deleteAccount(USER_ID);

        assertThatThrownBy(() -> userController.deleteAccount(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteAccount_shouldThrow_whenAccessDenied() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtil).verifyOwnershipOrAdmin(USER_ID);

        assertThatThrownBy(() -> userController.deleteAccount(USER_ID))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(userService, never()).deleteAccount(any());
    }
}
