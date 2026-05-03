package global_pass.auth;

import global_pass.users.User;
import global_pass.users.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private static final String HS512_HEADER = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9";
    private static final String VALID_TOKEN = HS512_HEADER + ".payload.signature";
    private static final String EMAIL = "test@mail.com";
    private static final String ROLE = "USER";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    // ──────────────────────────────────────────────
    // No header
    // ──────────────────────────────────────────────

    @Test
    void doFilterInternal_noAuthHeader_doesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userRepository);
    }

    @Test
    void doFilterInternal_headerWithoutBearer_doesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userRepository);
    }

    // ──────────────────────────────────────────────
    // Non-HS512 token (Google/Supabase) — skipped
    // ──────────────────────────────────────────────

    @Test
    void doFilterInternal_googleToken_isSkipped() throws Exception {
        String es256Header = "eyJhbGciOiJFUzI1NiJ9";
        String googleToken = es256Header + ".payload.signature";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + googleToken);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userRepository);
    }

    // ──────────────────────────────────────────────
    // Invalid HS512 token
    // ──────────────────────────────────────────────

    @Test
    void doFilterInternal_invalidToken_doesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractEmail(any());
        verifyNoInteractions(userRepository);
    }

    @Test
    void doFilterInternal_invalidToken_doesNotCallExtractEmail() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil, never()).extractEmail(any());
        verifyNoInteractions(userRepository);
    }

    // ──────────────────────────────────────────────
    // Valid token — user not in DB
    // ──────────────────────────────────────────────

    @Test
    void doFilterInternal_validToken_authenticatesUser() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        // ← no extractIssuedAt — user is null so password check is skipped

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(EMAIL);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validToken_setsCorrectRole() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void doFilterInternal_validToken_setsAdminRole() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn("ADMIN");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void doFilterInternal_validToken_setsDefaultUserRole_whenRoleIsNull() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void doFilterInternal_validToken_marksRequestAsAuthenticated() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        // ← no extractIssuedAt — not called when user is null

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("jwt_authenticated", true);
    }

    @Test
    void doFilterInternal_validToken_stillContinuesFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_userNotFoundInDb_authenticates() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
    }

    // ──────────────────────────────────────────────
    // Password change invalidation
    // ──────────────────────────────────────────────

    @Test
    void doFilterInternal_tokenIssuedBeforePasswordChange_doesNotAuthenticate() throws Exception {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordChangedAt(LocalDateTime.now());

        // token issued 1 hour ago — before password change
        Date issuedAt = new Date(System.currentTimeMillis() - 3600_000);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
        when(jwtUtil.extractIssuedAt(VALID_TOKEN)).thenReturn(issuedAt); // ← needed here
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenIssuedAfterPasswordChange_authenticates() throws Exception {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordChangedAt(LocalDateTime.now().minusHours(1));

        // token issued NOW — after password change
        Date issuedAt = new Date();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
        when(jwtUtil.extractIssuedAt(VALID_TOKEN)).thenReturn(issuedAt); // ← needed here
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(EMAIL);
    }

    @Test
    void doFilterInternal_userHasNoPasswordChangedAt_authenticates() throws Exception {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordChangedAt(null); // ← never changed password

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractEmail(VALID_TOKEN)).thenReturn(EMAIL);
        when(jwtUtil.extractRole(VALID_TOKEN)).thenReturn(ROLE);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        // ← no extractIssuedAt — passwordChangedAt is null so check is skipped

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(EMAIL);
    }
}
