package se.sowl.progdomain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    List<Post> findByCreatedAtBefore(LocalDateTime oneMonthAgo);

    Page<Post> findAllByIdInAndDeletedFalse(List<Long> id, Pageable pageable);

    boolean existsByIdAndDeletedFalse(Long id);
}
