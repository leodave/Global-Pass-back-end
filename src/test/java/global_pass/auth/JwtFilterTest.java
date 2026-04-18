package global_pass.auth;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    // --- no header ---

    @Test
    void doFilterInternal_noAuthHeader_doesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        // Security context should remain empty
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        // Filter chain should still continue
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_noAuthHeader_stillContinuesFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // --- wrong header format ---

    @Test
    void doFilterInternal_headerWithoutBearer_doesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emptyBearerToken_doesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(jwtUtil.isTokenValid("")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // --- invalid token ---

    @Test
    void doFilterInternal_invalidToken_doesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        when(jwtUtil.isTokenValid("invalidtoken")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_doesNotCallExtractEmail() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        when(jwtUtil.isTokenValid("invalidtoken")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        // Should never try to extract email from an invalid token
        verify(jwtUtil, never()).extractEmail("invalidtoken");
    }

    // --- valid token ---

    @Test
    void doFilterInternal_validToken_authenticatesUser() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
        when(jwtUtil.isTokenValid("validtoken")).thenReturn(true);
        when(jwtUtil.extractEmail("validtoken")).thenReturn("test@mail.com");

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("test@mail.com");
    }

    @Test
    void doFilterInternal_validToken_stillContinuesFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
        when(jwtUtil.isTokenValid("validtoken")).thenReturn(true);
        when(jwtUtil.extractEmail("validtoken")).thenReturn("test@mail.com");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validToken_setsEmptyAuthorities() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
        when(jwtUtil.isTokenValid("validtoken")).thenReturn(true);
        when(jwtUtil.extractEmail("validtoken")).thenReturn("test@mail.com");

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities()).isEmpty();
    }
}
