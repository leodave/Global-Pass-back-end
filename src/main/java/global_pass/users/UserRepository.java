package global_pass.users;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// DB access for User entity — Spring Data JPA auto-generates queries
public interface UserRepository extends JpaRepository<User, Long> {

    // Used for login — find user by email
    Optional<User> findByEmail(String email);

    // Used for signup/update — check if email is already taken
    boolean existsByEmail(String email);
}
