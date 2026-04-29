package global_pass.auth.oath2;

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

        // Return null if email missing — base class will skip processing
        if (email == null) {
            log.warn("Google OAuth2 token missing email claim");
            return null;
        }

        return new OAuthUserInfo("Google", email);
    }
}
