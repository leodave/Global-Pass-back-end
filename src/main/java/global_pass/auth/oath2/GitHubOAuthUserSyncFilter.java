/*
package global_pass.auth.oath2;

import global_pass.users.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

// example of how easy it is to add a new provider in the future
@Component
@Slf4j
public class GitHubOAuthUserSyncFilter extends OAuthUserSyncFilter {

    public GitHubOAuthUserSyncFilter(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    protected OAuthUserInfo extractUserInfo(JwtAuthenticationToken jwtAuth) {

        // GitHub uses different claim names than Google
        String email = jwtAuth.getToken().getClaimAsString("user_email");

        if (email == null) {
            log.warn("GitHub OAuth2 token missing email claim");
            return null;
        }

        return new OAuthUserInfo("GitHub", email);
    }
}
*/
