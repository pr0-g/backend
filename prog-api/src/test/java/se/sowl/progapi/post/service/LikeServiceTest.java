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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;

@SpringBootTest
class LikeServiceTest {

    private static RedisServer redisServer;
    private static int redisPort;

    @Autowired
    private PostViewService postViewService;

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
        Objects.requireNonNull(postViewService.getRedisTemplate().getConnectionFactory())
            .getConnection()
            .serverCommands()
            .flushAll();
    }

    // addLike
    //

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
