package se.sowl.progapi.post.service;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progdomain.post.domain.Like;
import se.sowl.progdomain.post.repository.LikeRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class LikeService {
    @Getter
    private final RedisTemplate<String, String> redisTemplate;
    private final LikeRepository likeRepository;
    private final String POST_LIKE_COUNT_PRESET = "post:likes_count:";

    @Transactional
    public void addLike(Long postId, Long userId) {
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            likeRepository.save(new Like(postId, userId));
            incrementLikeCount(postId);
        }
    }

    public long getLikeCount(Long postId) {
        String key = POST_LIKE_COUNT_PRESET + postId;
        String cachedCount = redisTemplate.opsForValue().get(key);
        if (cachedCount != null) {
            return Long.parseLong(cachedCount);
        }
        return getLikeCountWithSet(postId, key);
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

    private long getLikeCountWithSet(Long postId, String key) {
        long count = likeRepository.countByPostId(postId);
        redisTemplate.opsForValue().set(key, String.valueOf(count));
        return count;
    }

    private void incrementLikeCount(Long postId) {
        String key = POST_LIKE_COUNT_PRESET + postId;
        redisTemplate.opsForValue().increment(key);
    }

//    @Transactional
//    public void removeLike(Long postId, Long userId) {
//        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
//            likeRepository.deleteByPostIdAndUserId(postId, userId);
//            decrementLikeCount(postId);
//        }
//    }

//    private void decrementLikeCount(Long postId) {
//        String key = POST_LIKE_COUNT_PRESET + postId;
//        redisTemplate.opsForValue().decrement(key);
//    }
}
