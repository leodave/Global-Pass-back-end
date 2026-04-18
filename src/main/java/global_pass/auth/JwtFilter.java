package global_pass.auth;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

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

                // Extract email from the token
                String email = jwtUtil.extractEmail(token);

                // Create an authentication object with the email and no roles/credentials
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, List.of());

                // Register the authentication in the security context so Spring knows the user is authenticated
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Continue to the next filter in the chain regardless of token presence
        filterChain.doFilter(request, response);
    }
}
