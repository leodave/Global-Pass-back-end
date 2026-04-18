package global_pass.auth;

import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Utility class for generating and validating JWT tokens
@Component
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
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getKey())           // Use the same key to verify signature
                .build()
                .parseSignedClaims(token)       // Parse and validate the token
                .getPayload()
                .getSubject();                  // Get the email we stored as subject
    }

    // Returns true if the token is valid, false if expired or tampered
    public boolean isTokenValid(String token) {
        try {
            extractEmail(token);    // If this doesn't throw, token is valid
            return true;
        } catch (Exception e) {
            return false;           // Token is expired, malformed, or tampered
        }
    }

}
