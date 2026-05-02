package global_pass.config;

import java.net.URL;

import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import global_pass.auth.JwtFilter;
import global_pass.auth.oath2.GoogleOAuthUserSyncFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
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

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${supabase.jwk-uri}")
    private String supabaseJwkUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Explicitly configure ES256 for Supabase/Google tokens
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(supabaseJwkUri));
        jwtProcessor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, jwkSource)
        );
        JwtDecoder supabaseDecoder = new NimbusJwtDecoder(jwtProcessor);

        http
                // Disable CSRF since we're using a stateless REST API (no cookies/sessions)
                .csrf(csrf -> csrf.disable())
                // Disable default Spring Security login page (not needed for REST API)
                .formLogin(form -> form.disable())
                // Stateless session — no server-side session stored, ready for JWT later
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Define endpoint access rules
            .authorizeHttpRequests(auth -> auth
                // Allow signup and login without authentication
                .requestMatchers("/public/api/auth/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

                 // Add our JWT filter before Spring's default username/password filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // handles Google/Supabase ES256 tokens
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {
                            // if already authenticated by JwtFilter, skip oauth2 processing
                            if (request.getAttribute("jwt_authenticated") != null) {
                                return null; // ← returning null skips BearerTokenAuthenticationFilter
                            }
                            // otherwise extract token normally for Google tokens
                            return new DefaultBearerTokenResolver().resolve(request);
                        })
                        .jwt(jwt -> jwt.decoder(supabaseDecoder))
                )

                // runs after JwtFilter — SecurityContext is fully populated at this point
                .addFilterAfter(googleOAuthUserSyncFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    // For your own HS512 tokens — used by JwtUtil directly, NOT by oauth2ResourceServer
    @Bean
    public JwtDecoder localJwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA512")
        ).build();
    }

    // BCrypt password encoder for hashing and verifying passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
