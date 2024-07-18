package se.sowl.progdomain.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.interest.domain.UserInterest;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
}
