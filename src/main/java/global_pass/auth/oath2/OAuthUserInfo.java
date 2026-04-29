package global_pass.auth.oath2;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthUserInfo {

    // the provider name e.g. "Google", "GitHub"
    private final String provider;

    // the user's email from the JWT claims
    private final String email;
}
