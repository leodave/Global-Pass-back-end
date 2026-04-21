package global_pass.config;

import javax.crypto.spec.SecretKeySpec;

import global_pass.auth.JwtFilter;
import global_pass.auth.oath2.GoogleOAuthUserSyncFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Security configuration for the application
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Inject the JWT filter to add it to the security chain
    private final JwtFilter jwtFilter;

    // inject the specific Google implementation
    private final GoogleOAuthUserSyncFilter googleOAuthUserSyncFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF since we're using a stateless REST API (no cookies/sessions)
            .csrf(csrf -> csrf.disable())

            // Disable default Spring Security login page (not needed for REST API)
            .formLogin(form -> form.disable())

            // Stateless session — no server-side session stored, ready for JWT later
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Define endpoint access rules
            .authorizeHttpRequests(auth -> auth
                // Allow signup and login without authentication
                .requestMatchers("/public/api/auth/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                 // Add our JWT filter before Spring's default username/password filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // runs after JwtFilter — SecurityContext is fully populated at this point
                .addFilterAfter(googleOAuthUserSyncFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // uses your local secret to validate tokens during dev/testing
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(
                        "your-very-long-secret-key-at-least-256-bits-long-for-hs256".getBytes(),
                        "HmacSHA256"
                )
        ).build();
    }

    // BCrypt password encoder for hashing and verifying passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
