package global_pass.users;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// User entity — maps to "users" table in the database
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    // Unique email — used for login
    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    // Stored as BCrypt hash, never exposed in responses
    @NotBlank
    private String password;

    // Default role is USER, admin is assigned manually
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // Used to deactivate account without deleting
    private boolean active = true;

    // Password reset token
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    // Email verification
    private boolean emailVerified = false;
    private String verificationToken;

    // Tracks when password was last changed — tokens issued before this are invalid
    private LocalDateTime passwordChangedAt;

    // Auto-set on creation
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Auto-updated on every save
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Role { USER, ADMIN }
}
