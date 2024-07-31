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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @DisplayName("좋아요 증감")
    class addLike {
        @Test
        @DisplayName("좋아요를 누르지 않은 게시물이라면 좋아요를 누르면 좋아요 수가 증가한다.")
        void incrementLikeCount() {
            // given
            Long postId = 0L;
            Long userId = 1L;

            // when
            likeService.addLike(postId, userId);

            // then
            long likeCount = likeService.getLikeCount(postId);
            assertEquals(1L, likeCount);
        }

        @Test
        @DisplayName("이미 좋아요를 누른 게시물에 좋아요를 다시 눌러도 좋아요 수는 증가하지 않는다.")
        void shouldNotIncrementLikeCountWhenAlreadyLiked() {
            // given
            Long postId = 2L;
            Long userId = 2L;
            likeService.addLike(postId, userId);

            // when
            likeService.addLike(postId, userId);

            // then
            long likeCount = likeService.getLikeCount(postId);
            assertEquals(1L, likeCount);
        }
    }

    @Nested
    @DisplayName("좋아요 조회")
    class getLikeCount {
        @Test
        @DisplayName("좋아요 수가 호출 횟수만큼 증가해야 한다.")
        void getLikeCountWithExistPostId() {
            // given
            Long postId = 3L;
            Long userId = 3L;
            likeService.addLike(postId, userId);
            Long postId2 = 4L;
            Long userId2 = 4L;
            likeService.addLike(postId2, userId2);

            // when
            long likeCount = likeService.getLikeCount(postId);

            // then
            assertEquals(1L, likeCount);
        }

        @Test
        @DisplayName("저장되지 않은 글 ID로 조회한다면 좋아요 수가 0 이여야만 한다.")
        void getLikeCountWithNotExistPostId() {
            // given
            Long postId = 999L;

            // when
            long likeCount = likeService.getLikeCount(postId);

            // then
            assertEquals(0L, likeCount);
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
