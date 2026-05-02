package global_pass.auth.oath2;

import java.util.Map;

import global_pass.users.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GoogleOAuthUserSyncFilter extends OAuthUserSyncFilter {

    // pass UserRepository up to the base class
    public GoogleOAuthUserSyncFilter(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    protected OAuthUserInfo extractUserInfo(JwtAuthenticationToken jwtAuth) {

        // Extract email from Google's JWT claims via Supabase
        String email = jwtAuth.getToken().getClaimAsString("email");
        // name is nested inside user_metadata
        Map<String, Object> userMetadata = jwtAuth.getToken().getClaim("user_metadata");

        String name = null;
        if (userMetadata != null) {
            name = (String) userMetadata.get("full_name");
            // fallback to name if full_name not present
            if (name == null) {
                name = (String) userMetadata.get("name");
            }
        }

        if (email == null) {
            log.warn("Google OAuth2 token missing email claim");
            return null;
        }

        log.info("Extracted name: {}, email: {}", name, email);
        return new OAuthUserInfo("Google", email, name);
    }
}
