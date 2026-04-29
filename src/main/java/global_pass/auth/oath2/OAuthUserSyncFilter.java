package global_pass.auth.oath2;

import java.io.IOException;

import global_pass.users.User;
import global_pass.users.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public abstract class OAuthUserSyncFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    // constructor injection since @RequiredArgsConstructor doesn't work on abstract classes
    protected OAuthUserSyncFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Get the current authentication object from Spring Security context
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // log what type of authentication is coming in
        log.info("Authentication type: {}",
                authentication != null ? authentication.getClass().getSimpleName() : "null");

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            log.info("JwtAuthenticationToken found, claims: {}", jwtAuth.getToken().getClaims());

            OAuthUserInfo userInfo = extractUserInfo(jwtAuth);
            log.info("Extracted userInfo: {}, provider: {}, email: {}", userInfo, userInfo.getProvider(), userInfo.getEmail());

            if (userInfo != null && userInfo.getEmail() != null) {
                userRepository.findByEmail(userInfo.getEmail()).orElseGet(() -> {
                    log.info("First {} login, creating user for email: {}",
                            userInfo.getProvider(), userInfo.getEmail());
                    User newUser = new User();
                    newUser.setEmail(userInfo.getEmail());
                    newUser.setName(userInfo.getEmail());
                    newUser.setAuthProvider(User.AuthProvider.valueOf(
                            userInfo.getProvider().toUpperCase()));
                    return userRepository.save(newUser);
                });
            }
        } else {
            log.info("No JwtAuthenticationToken found, skipping Google sync");
        }

        // Always continue the filter chain
        filterChain.doFilter(request, response);
    }

    // Each subclass implements this to extract user info from their provider's JWT claims
    protected abstract OAuthUserInfo extractUserInfo(JwtAuthenticationToken jwtAuth);
}
