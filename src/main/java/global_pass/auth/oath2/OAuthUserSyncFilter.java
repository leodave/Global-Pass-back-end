package global_pass.auth.oath2;

import java.io.IOException;

import global_pass.users.User;
import global_pass.users.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        log.info("Authentication type: {}",
                authentication != null ? authentication.getClass().getSimpleName() : "null");

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            // Google/Supabase token — existing logic
            OAuthUserInfo userInfo = extractUserInfo(jwtAuth);
            log.info("Extracted userInfo: {}, provider: {}, email: {}", userInfo, userInfo.getProvider(), userInfo.getEmail());
            if (userInfo.getEmail() != null) {
                syncUser(userInfo);
            }
        } else if (authentication instanceof UsernamePasswordAuthenticationToken upAuth) {
            // Your own JWT — email is the principal
            String email = (String) upAuth.getPrincipal();
            log.info("Regular JWT login for: {}", email);
            // no sync needed — user already exists in DB from signup
        }

        // Always continue the filter chain
        filterChain.doFilter(request, response);
    }

    private void syncUser(OAuthUserInfo userInfo) {
        userRepository.findByEmail(userInfo.getEmail()).orElseGet(() -> {
            log.info("First {} login, creating user: {}", userInfo.getProvider(), userInfo.getEmail());
            User newUser = new User();
            newUser.setEmail(userInfo.getEmail());
            newUser.setName(userInfo.getEmail());
            newUser.setName(userInfo.getName() != null ? userInfo.getName()
                    : userInfo.getEmail());
            newUser.setAuthProvider(User.AuthProvider.valueOf(
                    userInfo.getProvider().toUpperCase()));
            return userRepository.save(newUser);
        });
    }

    // Each subclass implements this to extract user info from their provider's JWT claims
    protected abstract OAuthUserInfo extractUserInfo(JwtAuthenticationToken jwtAuth);
}
