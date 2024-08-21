package se.sowl.progapi.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progapi.post.dto.PostResponse;
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
    private final PostService postService;

    @Transactional(readOnly = true)
    public Page<PostResponse> getLikedPosts(Long userId, Pageable pageable) {
        Page<Like> likesPage = likeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Long> postIds = likesPage.getContent().stream().map(Like::getPostId).toList();
        Page<Post> posts = postRepository.findAllByIdInAndDeletedFalse(postIds, pageable);
        return postService.toPagePostResponse(posts);
    }
}
