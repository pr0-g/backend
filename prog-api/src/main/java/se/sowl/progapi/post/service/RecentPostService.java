package se.sowl.progapi.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progapi.post.dto.RecentPostResponse;
import se.sowl.progapi.post.dto.PostSummary;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.PostRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentPostService {

    private final PostRepository recentPostRepository;
    private final LikeService likeService;

    @Transactional(readOnly = true)
    public RecentPostResponse getRecentPosts(Pageable pageable) {
        Page<Post> postsPage = recentPostRepository.findAllByDeletedFalseOrderByCreatedAtDesc(pageable);
        List<PostSummary> postSummaries = this.getPostSummary(postsPage);
        return RecentPostResponse.from(postSummaries, postsPage.getTotalElements(), postsPage.getTotalPages());
    }

    public List<PostSummary> getPostSummary(Page<Post> pages) {
        return pages.getContent().stream()
                .map(post -> {
                    long likeCount = likeService.getLikeCount(post.getId());
                    return PostSummary.from(post, likeCount);
                })
                .toList();
    }
}
