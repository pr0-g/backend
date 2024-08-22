package se.sowl.progdomain.user.repository;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndProvider(String email, String provider);
    Optional<User> findByEmail(String email);

    public default User findByIdOrThrow(Long id){
        return findById(id).orElseThrow(() -> new EntityNotFoundException(String.valueOf(id)));
    }
}

