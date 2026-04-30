package global_pass.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Inject values since @Value doesn't work in plain unit tests
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-that-is-at-least-32-characters-long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    // --- generateToken ---

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("test@mail.com", "USER");

        assertThat(token).isNotNull();
    }

    @Test
    void generateToken_returnsValidJwtFormat() {
        String token = jwtUtil.generateToken("test@mail.com", "USER");

        // JWT has 3 parts separated by dots
        assertThat(token.split("\\.")).hasSize(3);
    }

    // --- extractEmail ---

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken("test@mail.com", "USER");

        String email = jwtUtil.extractEmail(token);

        assertThat(email).isEqualTo("test@mail.com");
    }

    @Test
    void extractEmail_differentEmails_returnCorrectEmail() {
        String token1 = jwtUtil.generateToken("alice@mail.com", "USER");
        String token2 = jwtUtil.generateToken("bob@mail.com", "ADMIN");

        assertThat(jwtUtil.extractEmail(token1)).isEqualTo("alice@mail.com");
        assertThat(jwtUtil.extractEmail(token2)).isEqualTo("bob@mail.com");
    }

    // --- isTokenValid ---

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("test@mail.com", "USER");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("test@mail.com", "USER");
        String tampered = token + "tampered";

        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }

    @Test
    void isTokenValid_randomString_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("this.is.not.a.token")).isFalse();
    }

    @Test
    void isTokenValid_emptyString_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("")).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        // Set expiration to -1 so the token is already expired
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String expiredToken = jwtUtil.generateToken("test@mail.com", "USER");

        assertThat(jwtUtil.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void isTokenValid_tokenSignedWithDifferentSecret_returnsFalse() {
        // Generate token with original secret
        String token = jwtUtil.generateToken("test@mail.com", "USER");

        // Change the secret to simulate a different server
        ReflectionTestUtils.setField(jwtUtil, "secret", "completely-different-secret-key-32-chars!!");

        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }
}
