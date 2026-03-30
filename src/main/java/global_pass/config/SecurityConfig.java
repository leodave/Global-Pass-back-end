package global_pass.global_pass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
// Security configuration for the application
@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
                .requestMatchers("/api/auth/**").permitAll()
                // TODO: protect these with JWT later
                .requestMatchers("/api/users/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }

    // BCrypt password encoder for hashing and verifying passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
