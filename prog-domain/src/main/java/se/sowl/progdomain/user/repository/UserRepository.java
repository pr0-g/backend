package se.sowl.progdomain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    Optional<User> findByEmailAndProvider(String email, String provider);
}
