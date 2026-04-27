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

    // Generates a JWT token with the user's email and role
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
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

    // Extracts the role from a valid JWT token
    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public Date extractIssuedAt(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getIssuedAt();
    }

    // Returns true if the token is valid, false if expired or tampered
    public boolean isTokenValid(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
