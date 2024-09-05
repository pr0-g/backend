package se.sowl.progapi.post.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import redis.embedded.RedisServer;
import se.sowl.progapi.post.dto.PostResponse;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.repository.InterestRepository;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.PostRepository;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private InterestRepository interestRepository;

    @MockBean
    private PostViewService postViewService;

    @MockBean
    private LikeService likeService;

    private static RedisServer redisServer;
    private static int redisPort;

    private static StringRedisTemplate redisTemplate;

    private static final String TRENDING_POSTS_KEY = "posts:trending";

    private List<Interest> interests;

    @BeforeAll
    static void setUpRedisServer() throws IOException {
        redisPort = findAvailablePort();
        redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("maxmemory 128M")
                .build();
        redisServer.start();
    }

    @AfterAll
    static void tearDownRedisServer() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        redisTemplate = trendingPostService.getRedisTemplate();
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushAll();
        postRepository.deleteAll();
        interestRepository.deleteAll();
        interests = createInterests(10);
        when(postViewService.getViewCount(anyLong())).thenReturn(0L);
        when(likeService.getLikeCount(anyLong())).thenReturn(0L);
    }

    private List<Interest> createInterests(int count) {
        List<Interest> interestList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            interestList.add(new Interest("Interest " + (i + 1)));
        }
        return interestRepository.saveAll(interestList);
    }

    private List<Post> createTestPosts(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Post post = Post.builder()
                    .title("Test Post " + i)
                    .userId((long) (i % 5 + 1))
                    .interest(interests.get(i % interests.size()))
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            posts.add(post);
        }
        return postRepository.saveAll(posts);
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
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<PostResponse> result = trendingPostService.getTrendingPosts(pageable);

            // then
            assertThat(result.getContent()).hasSize(10);
        }

        @Test
        @DisplayName("트렌딩 게시물이 없는 경우 빈 리스트를 반환한다")
        void getTrendingPostsWhenNoPostsExist() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<PostResponse> result = trendingPostService.getTrendingPosts(pageable);

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

    private static int findAvailablePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.port", () -> redisPort);
        registry.add("spring.data.redis.host", () -> "localhost");
    }

    @TestConfiguration
    static class TestRedisConfiguration {
        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("localhost", redisPort);
            return new LettuceConnectionFactory(configuration);
        }
    }
}