package se.sowl.progdomain.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.interest.domain.Interest;

public interface InterestRepository extends JpaRepository<Interest, Long> {
}
