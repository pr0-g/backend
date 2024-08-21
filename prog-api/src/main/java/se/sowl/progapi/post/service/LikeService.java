package se.sowl.progapi.post.service;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progapi.post.exception.PostException;
import se.sowl.progdomain.post.domain.Like;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.LikeRepository;
import se.sowl.progdomain.post.repository.PostRepository;

import java.util.Optional;
import java.util.Set;

@Service
public class LikeService {
    @Getter
    private final RedisTemplate<String, String> redisTemplate;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final String POST_LIKE_COUNT_PRESET = "post:likes_count:";

    @Autowired
    public LikeService(
            RedisTemplate<String, String> redisTemplate,
            LikeRepository likeRepository,
            PostRepository postRepository,
            @Lazy PostService postService
    ) {
        this.redisTemplate = redisTemplate;
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.postService = postService;
    }


    @Transactional
    public boolean toggleLike(Long postId, Long userId) {

        if (!postService.existsPost(postId)) {
            throw new PostException.PostNotExistException();
        }

        return likeRepository.findByPostIdAndUserId(postId, userId)
                .map(like -> {
                    likeRepository.delete(like);
                    updateLikeCountCache(postId, -1);
                    return false;
                })
                .orElseGet(() -> {
                    likeRepository.save(new Like(postId, userId));
                    updateLikeCountCache(postId, 1);
                    return true;
                });
    }

    public long getLikeCount(Long postId) {
        String key = POST_LIKE_COUNT_PRESET + postId;
        String cachedCount = redisTemplate.opsForValue().get(key);
        if (cachedCount != null) {
            long count = Long.parseLong(cachedCount);
            long dbCount = likeRepository.countByPostId(postId);

            if (count != dbCount) {
                redisTemplate.opsForValue().set(key, String.valueOf(dbCount));
                return dbCount;
            }
            return count;
        }
        return getLikeCountWithSet(postId, key);
    }

    private void updateLikeCountCache(Long postId, int delta) {
        String key = POST_LIKE_COUNT_PRESET + postId;
        redisTemplate.opsForValue().increment(key, delta);
    }

    private long getLikeCountWithSet(Long postId, String key) {
        long count = likeRepository.countByPostId(postId);
        redisTemplate.opsForValue().set(key, String.valueOf(count));
        return count;
    }

    @Scheduled(fixedRate = 3600000)
    public void syncLikeCountCache() {
        Set<String> keys = redisTemplate.keys(POST_LIKE_COUNT_PRESET + "*");
        for (String key : keys) {
            String postIdStr = key.substring(POST_LIKE_COUNT_PRESET.length());
            Long postId = Long.parseLong(postIdStr);
            long dbCount = likeRepository.countByPostId(postId);
            redisTemplate.opsForValue().set(key, String.valueOf(dbCount));
        }
    }

    public boolean hasUserLiked(Long postId, Long userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }


//    @Scheduled(fixedRate = 600000)
//    @Transactional
//    public void scheduledSyncLikeCountsToDatabase() {
//        Set<String> keys = redisTemplate.keys(POST_LIKE_COUNT_PRESET + "*");
//        if (keys == null) return;
//        keys.forEach(this::syncSinglePostLikeCount);
//    }
//
//    private void syncSinglePostLikeCount(String key) {
//        Long postId = extractPostIdFromKey(key);
//        Long redisCount = getRedisCount(key);
//        if (redisCount == null) return;
//        updateRedisIfNeeded(key, postId, redisCount);
//    }
//
//    private Long extractPostIdFromKey(String key) {
//        return Long.parseLong(key.split(":")[2]);
//    }
//
//    private Long getRedisCount(String key) {
//        String countStr = redisTemplate.opsForValue().get(key);
//        return countStr != null ? Long.parseLong(countStr) : null;
//    }
//
//    private void updateRedisIfNeeded(String key, Long postId, Long redisCount) {
//        long dbCount = likeRepository.countByPostId(postId);
//        if (redisCount != dbCount) {
//            redisTemplate.opsForValue().set(key, String.valueOf(dbCount));
//        }
//    }


    private void incrementLikeCount(Long postId) {
        String key = POST_LIKE_COUNT_PRESET + postId;
        redisTemplate.opsForValue().increment(key);
    }

//    @Transactional
//    public void removeLike(Long postId, Long userId) {
//        if (
//          likeRepository.existsByPostIdAndUserId(postId, userId)
//        ) {
//            likeRepository.deleteByPostIdAndUserId(postId, userId);
//            decrementLikeCount(postId);
//        }
//    }

//    private void decrementLikeCount(Long postId) {
//        String key = POST_LIKE_COUNT_PRESET + postId;
//        redisTemplate.opsForValue().decrement(key);
//    }
}
