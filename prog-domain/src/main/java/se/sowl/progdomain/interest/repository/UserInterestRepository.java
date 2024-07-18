package se.sowl.progdomain.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.interest.domain.UserInterest;

import java.util.List;
import java.util.Optional;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    Optional<List<UserInterest>> findAllByUserId(Long userId);
    void deleteAllByUserId(Long userId);
}
