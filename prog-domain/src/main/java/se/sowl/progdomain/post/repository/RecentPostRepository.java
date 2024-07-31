package se.sowl.progdomain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.post.domain.Post;

public interface RecentPostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
}