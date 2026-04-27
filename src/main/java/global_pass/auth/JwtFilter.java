package global_pass.auth;

import java.io.IOException;
import java.util.List;

import global_pass.users.User;
import global_pass.users.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Get the Authorization header from the request
        String authHeader = request.getHeader("Authorization");

        // Check if the header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Strip "Bearer " prefix to get the raw token
            String token = authHeader.substring(7);

            // Validate the token
            if (jwtUtil.isTokenValid(token)) {

                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // Reject tokens issued before password change
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null && user.getPasswordChangedAt() != null) {
                    java.util.Date issuedAt = jwtUtil.extractIssuedAt(token);
                    java.time.LocalDateTime tokenTime = issuedAt.toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                    if (tokenTime.isBefore(user.getPasswordChangedAt())) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                }

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER"))
                );

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                // Register the authentication in the security context so Spring knows the user is authenticated
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Continue to the next filter in the chain regardless of token presence
        filterChain.doFilter(request, response);
    }
}
