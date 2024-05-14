package se.sowl.progdomain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
