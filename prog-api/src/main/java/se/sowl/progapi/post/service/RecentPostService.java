package se.sowl.progapi.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progapi.post.dto.PostResponse;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class RecentPostService {

    private final PostRepository postRepository;
    private final PostService postService;

    @Transactional(readOnly = true)
    public Page<PostResponse> getRecentPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findAllByDeletedFalseOrderByCreatedAtDesc(pageable);
        return postService.toPagePostResponse(postsPage);
    }
}
