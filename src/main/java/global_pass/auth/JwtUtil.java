package global_pass.auth;

import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Utility class for generating and validating JWT tokens
@Component
@Slf4j
public class JwtUtil {

    // Secret key
    @Value("${jwt.secret}")
    private String secret;

    // Token expiration time in milliseconds
    @Value("${jwt.expiration}")
    private long expiration;

    // Converts the secret string into a secure signing key
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generates a JWT token with the user's email as the subject
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)                                                      // Store email inside the token
                .issuedAt(new Date())                                                // Token creation time
                .expiration(new Date(System.currentTimeMillis() + expiration))      // Token expiry time
                .signWith(getKey())                                                  // Sign with secret key
                .compact();                                                          // Build the token string
    }

    // Extracts the email from a valid JWT token
    // handles both your own tokens (email as subject) and Google/Supabase tokens (email as claim)
    public String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Try email claim first (Google/Supabase tokens)
        String email = claims.get("email", String.class);

        // Fall back to subject (your own JWT tokens)
        if (email == null) {
            email = claims.getSubject();
        }

        return email;
    }
    // Returns true if the token is valid, false if expired or tampered
    public boolean isTokenValid(String token) {
            try {
                extractEmail(token);
                log.info("user with email: {} is making a request", extractEmail(token));
                return true;
            } catch (Exception e) {
                log.error("email couldn't be extracted from token because of {}", e.getMessage());
                return false;
            }
    }

}
