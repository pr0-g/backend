package se.sowl.progapi.post.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.PostRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class TrendingPostServiceTest {

    @Autowired
    private TrendingPostService trendingPostService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockBean
    private PostViewService postViewService;

    @MockBean
    private LikeService likeService;

    private static final String TRENDING_POSTS_KEY = "posts:trending";

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        redisTemplate.delete(TRENDING_POSTS_KEY);
        when(postViewService.getViewCount(anyLong())).thenReturn(0L);
        when(likeService.getLikeCount(anyLong())).thenReturn(0L);
    }

    @Nested
    @DisplayName("getTrendingPosts 메서드 테스트")
    class GetTrendingPostsTest {

        @Test
        @DisplayName("트렌딩 게시물을 페이지별로 조회할 수 있다")
        void getTrendingPosts() {
            // given
            List<Post> posts = createTestPosts(50);
            for (Post post : posts) {
                when(postViewService.getViewCount(post.getId())).thenReturn(100L);
                when(likeService.getLikeCount(post.getId())).thenReturn(50L);
                trendingPostService.updatePostScore(post.getId());
            }

            // when
            List<Post> result = trendingPostService.getTrendingPosts(0, 10);

            // then
            assertThat(result).hasSize(10);
        }

        @Test
        @DisplayName("트렌딩 게시물이 없는 경우 빈 리스트를 반환한다")
        void getTrendingPostsWhenNoPostsExist() {
            // given && when
            List<Post> result = trendingPostService.getTrendingPosts(0, 10);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updatePostScore 메서드 테스트")
    class UpdatePostScoreTest {

        @Test
        @DisplayName("게시물 점수를 업데이트할 수 있다")
        void updatePostScore() {
            // given
            Post post = createTestPosts(1).get(0);
            when(postViewService.getViewCount(post.getId())).thenReturn(100L);
            when(likeService.getLikeCount(post.getId())).thenReturn(50L);

            // when
            trendingPostService.updatePostScore(post.getId());

            // then
            Double score = redisTemplate.opsForZSet().score(TRENDING_POSTS_KEY, post.getId().toString());
            assertThat(score).isEqualTo(200.0); // 100 * 1.0 + 50 * 2.0
        }
    }

    @Nested
    @DisplayName("cleanupOldPosts 메서드 테스트")
    class CleanupOldPostsTest {

        @Test
        @DisplayName("한 달 이상 지난 게시물을 정리할 수 있다")
        void cleanupOldPosts() throws NoSuchFieldException, IllegalAccessException {
            // given
            List<Post> oldPosts = createTestPosts(15);
            for (Post post : oldPosts) {
                Field field = Post.class.getDeclaredField("createdAt");
                field.setAccessible(true);
                field.set(post, LocalDateTime.now().minusMonths(2));
            }
            postRepository.saveAll(oldPosts);

            List<Post> recentPosts = createTestPosts(5);
            postRepository.saveAll(recentPosts);

            List<Post> allPosts = new ArrayList<>(oldPosts);
            allPosts.addAll(recentPosts);

            for (Post post : allPosts) {
                trendingPostService.updatePostScore(post.getId());
            }

            // when
            trendingPostService.cleanupOldPosts();

            // then
            Set<String> remainingPosts = redisTemplate.opsForZSet().range(TRENDING_POSTS_KEY, 0, -1);
            assertThat(remainingPosts).hasSize(5);
            for (Post post : recentPosts) {
                assertThat(remainingPosts).contains(post.getId().toString());
            }
            for (Post post : oldPosts) {
                assertThat(remainingPosts).doesNotContain(post.getId().toString());
            }
        }
    }

    private List<Post> createTestPosts(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Post post = Post.builder()
                .title("Test Post " + i)
                .userId((long) (i % 5 + 1))
                .interestId((long) (i % 10 + 1))
                .createdAt(LocalDateTime.now().minusDays(i))
                .build();
            posts.add(post);
        }
        return postRepository.saveAll(posts);
    }
}
