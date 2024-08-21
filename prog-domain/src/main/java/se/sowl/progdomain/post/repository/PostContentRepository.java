package se.sowl.progdomain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sowl.progdomain.post.domain.PostContent;

import java.util.Optional;

public interface PostContentRepository extends JpaRepository<PostContent, Long> {
    Optional<PostContent> findByPostId(Long postId);
}