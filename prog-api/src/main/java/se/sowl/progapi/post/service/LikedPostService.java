package se.sowl.progapi.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progapi.post.dto.LikedPostResponse;
import se.sowl.progapi.post.dto.PostSummary;
import se.sowl.progdomain.post.domain.Like;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.LikeRepository;
import se.sowl.progdomain.post.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikedPostService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final LikeService likeService;

    @Transactional(readOnly = true)
    public LikedPostResponse getLikedPosts(Long userId, Pageable pageable) {
        Page<Like> likesPage = likeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<PostSummary> posts = getLikedPostSummary(likesPage);
        return LikedPostResponse.from(posts, likesPage.getTotalElements(), likesPage.getTotalPages());
    }

    private List<PostSummary> getLikedPostSummary(Page<Like> likesPage) {
        List<Long> postIds = likesPage.getContent().stream().map(Like::getPostId).toList();
        List<Post> posts = postRepository.findAllByIdInAndDeletedFalse(postIds);
        return posts.stream()
                .map(post -> {
                    long likeCount = likeService.getLikeCount(post.getId());
                    return PostSummary.from(post, likeCount);
                })
                .toList();
    }
}
