package se.sowl.progapi.post.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostViewService {
    @Getter
    private final RedisTemplate<String, String> redisTemplate;
    private final String POST_VIEW_PRESET = "post:views:";

    public void incrementViewCount(Long postId) {
        String key = POST_VIEW_PRESET + postId;
        redisTemplate.opsForValue().increment(key);
    }

    public Long getViewCount(Long postId) {
        String key = POST_VIEW_PRESET + postId;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : 0L;
    }
}
