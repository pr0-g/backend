package se.sowl.progapi.post.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import redis.embedded.RedisServer;
import se.sowl.progdomain.post.repository.LikeRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LikeServiceTest {

    private static RedisServer redisServer;
    private static int redisPort;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeService likeService;

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
        Objects.requireNonNull(likeService.getRedisTemplate().getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @AfterEach
    void tearDown() {
        likeRepository.deleteAll();
    }

    @Nested
    @DisplayName("좋아요 토글")
    class ToggleLike {
        @Test
        @DisplayName("좋아요를 누르지 않은 게시물이라면 좋아요를 누르면 true를 반환하고 좋아요 수가 증가한다.")
        void toggleLikeForFirstTime() {
            // given
            Long postId = 1L;
            Long userId = 1L;

            // when
            boolean result = likeService.toggleLike(postId, userId);

            // then
            assertTrue(result);
            assertEquals(1L, likeService.getLikeCount(postId));
            assertTrue(likeService.hasUserLiked(postId, userId));
        }

        @Test
        @DisplayName("이미 좋아요를 누른 게시물에 좋아요를 다시 누르면 false를 반환하고 좋아요 수가 감소한다.")
        void toggleLikeForSecondTime() {
            // given
            Long postId = 2L;
            Long userId = 2L;
            likeService.toggleLike(postId, userId);

            // when
            boolean result = likeService.toggleLike(postId, userId);

            // then
            assertFalse(result);
            assertEquals(0L, likeService.getLikeCount(postId));
            assertFalse(likeService.hasUserLiked(postId, userId));
        }
    }

    @Nested
    @DisplayName("좋아요 조회")
    class GetLikeCount {
        @Test
        @DisplayName("좋아요 수가 정확히 반영되어야 한다.")
        void getLikeCountWithExistPostId() {
            // given
            Long postId = 3L;
            Long userId1 = 3L;
            Long userId2 = 4L;
            likeService.toggleLike(postId, userId1);
            likeService.toggleLike(postId, userId2);

            // when
            long likeCount = likeService.getLikeCount(postId);

            // then
            assertEquals(2L, likeCount);
        }

        @Test
        @DisplayName("저장되지 않은 글 ID로 조회하면 좋아요 수가 0 이어야 한다.")
        void getLikeCountWithNotExistPostId() {
            // given
            Long postId = 999L;

            // when
            long likeCount = likeService.getLikeCount(postId);

            // then
            assertEquals(0L, likeCount);
        }
    }

    @Test
    @DisplayName("사용자의 좋아요 상태 확인")
    void checkUserLikeStatus() {
        // given
        Long postId = 4L;
        Long userId = 5L;

        // when
        likeService.toggleLike(postId, userId);

        // then
        assertTrue(likeService.hasUserLiked(postId, userId));

        // when
        likeService.toggleLike(postId, userId);

        // then
        assertFalse(likeService.hasUserLiked(postId, userId));
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