package se.sowl.progapi.post.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.sowl.progapi.post.dto.PostResponse;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrendingPostService {
    @Getter
    private final StringRedisTemplate redisTemplate;
    private final PostViewService postViewService;
    private final LikeService likeService;
    private final PostService postService;
    private final PostRepository postRepository;

    private static final String TRENDING_POSTS_KEY = "posts:trending";
    private static final int TRENDING_POSTS_COUNT = 100;
    private static final double VIEW_WEIGHT = 1.0;
    private static final double LIKE_WEIGHT = 2.0;

    public void updatePostScore(Long postId) {
        double score = getScore(postId);
        redisTemplate.opsForZSet().add(TRENDING_POSTS_KEY, postId.toString(), score);
        redisTemplate.opsForZSet().removeRange(TRENDING_POSTS_KEY, 0, -TRENDING_POSTS_COUNT - 1);
    }

    public Page<PostResponse> getTrendingPosts(Pageable pageable) {
        List<Long> postIdList = this.getPostIds(pageable.getPageNumber(), pageable.getPageSize());
        Page<Post> postPages = postRepository.findAllByIdInAndDeletedFalse(postIdList, pageable);
        return postService.toPagePostResponse(postPages);
    }

    private List<Long> getPostIds(long start, long end) {
        Set<String> postIds = redisTemplate.opsForZSet().reverseRange(TRENDING_POSTS_KEY, start, end);
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postIds.stream().map(Long::parseLong).collect(Collectors.toList());
    }

    private double getScore(Long postId) {
        long views = postViewService.getViewCount(postId);
        long likes = likeService.getLikeCount(postId);
        return calculateScore(views, likes);
    }

    private double calculateScore(long views, long likes) {
        return (views * VIEW_WEIGHT) + (likes * LIKE_WEIGHT);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupOldPosts() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Post> oldPosts = postRepository.findByCreatedAtBefore(oneMonthAgo);
        for (Post post : oldPosts) {
            redisTemplate.opsForZSet().remove(TRENDING_POSTS_KEY, post.getId().toString());
        }
    }
}
