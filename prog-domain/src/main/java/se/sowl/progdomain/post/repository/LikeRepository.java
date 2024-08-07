package se.sowl.progdomain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.post.domain.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    void deleteByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    Page<Like> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
