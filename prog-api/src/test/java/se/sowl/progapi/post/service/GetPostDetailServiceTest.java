package se.sowl.progapi.post.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.progapi.fixture.PostFixture;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.post.dto.PostDetailResponse;
import se.sowl.progapi.post.exception.PostException;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.repository.InterestRepository;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.PostRepository;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GetPostDetailServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private LikeService likeService;

    private User testUser;
    private Interest testInterest;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(UserFixture.createUser(null, "Test User", "TestNick", "test@example.com", "testProvider"));
        testInterest = interestRepository.save(PostFixture.createInterest(null, "Test Interest"));
        testPost = postRepository.save(PostFixture.createPost(null, "Test Title", testUser.getId(), testInterest, "test.jpg", "Test Content"));
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        interestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("존재하는 게시글의 상세 정보를 정확히 조회한다")
    void getExistingPostDetail() {
        // given
        likeService.toggleLike(testPost.getId(), testUser.getId());

        // when
        PostDetailResponse response = postService.getPostDetail(testUser.getId(), testPost.getId());

        // then
        assertNotNull(response);
        assertEquals(testPost.getTitle(), response.getTitle());
        assertEquals(testPost.getPostContent().getContent(), response.getContent());
        assertEquals(1L, response.getLikeCount());
        assertEquals(testUser.getNickname(), response.getWriterNickname());
        assertEquals(testPost.getInterest().getId(), response.getInterest().getId());
        assertTrue(response.isUserLiked());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외가 발생한다")
    void getNonExistentPostDetail() {
        // given
        Long nonExistentPostId = 999L;

        // when & then
        PostException.PostNotExistException exception = assertThrows(PostException.PostNotExistException.class,
                () -> postService.getPostDetail(testUser.getId(), nonExistentPostId));
        assertEquals("존재하지 않는 게시글입니다.", exception.getMessage());
    }
}