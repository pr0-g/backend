package se.sowl.progapi.post.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.progapi.fixture.PostFixture;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.post.dto.EditPostRequest;
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
public class EditPostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestRepository interestRepository;

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
    @DisplayName("새 게시글을 성공적으로 생성한다")
    void createNewPost() {
        // given
        EditPostRequest request = new EditPostRequest(null, "New Title", "New Content", testInterest.getId(), "thumbnail.jpg");

        // when
        PostDetailResponse response = postService.editPost(testUser.getId(), request);

        // then
        assertNotNull(response);
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getContent(), response.getContent());
        assertEquals(testInterest.getId(), response.getInterest().getId());
        assertEquals(request.getThumbnailUrl(), response.getThumbnailUrl());
        assertEquals(0L, response.getLikeCount());

        Post savedPost = postRepository.findById(response.getId()).orElse(null);
        assertNotNull(savedPost);
        assertEquals(request.getTitle(), savedPost.getTitle());
        assertEquals(request.getContent(), savedPost.getPostContent().getContent());
        assertEquals(testInterest.getId(), savedPost.getInterest().getId());
    }

    @Test
    @DisplayName("기존 게시글을 성공적으로 수정한다")
    void updateExistingPost() {
        // given
        EditPostRequest request = new EditPostRequest(testPost.getId(), "Updated Title", "Updated Content", testInterest.getId(), "new_thumbnail.jpg");

        // when
        PostDetailResponse response = postService.editPost(testUser.getId(), request);

        // then
        assertNotNull(response);
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getContent(), response.getContent());
        assertEquals(request.getInterestId(), response.getInterest().getId());
        assertEquals(request.getThumbnailUrl(), response.getThumbnailUrl());

        Post updatedPost = postRepository.findById(testPost.getId()).orElse(null);
        assertNotNull(updatedPost);
        assertEquals(request.getTitle(), updatedPost.getTitle());
        assertEquals(request.getContent(), updatedPost.getPostContent().getContent());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 예외가 발생한다")
    void updateNonExistentPost() {
        // given
        Long nonExistentPostId = 999L;
        EditPostRequest request = new EditPostRequest(nonExistentPostId, "Title", "Content", testInterest.getId(), "thumbnail.jpg");

        // when & then
        assertThrows(PostException.PostNotExistException.class, () -> postService.editPost(testUser.getId(), request));
    }

    @Test
    @DisplayName("권한이 없는 사용자가 게시글 수정 시 예외가 발생한다")
    void updatePostWithoutAuthorization() {
        // given
        User anotherUser = userRepository.save(UserFixture.createUser(null, "Another User", "AnotherNick", "another@example.com", "testProvider"));
        EditPostRequest request = new EditPostRequest(testPost.getId(), "Updated Title", "Updated Content", testInterest.getId(), "new_thumbnail.jpg");

        // when & then
        assertThrows(PostException.PostNotAuthorizedException.class, () -> postService.editPost(anotherUser.getId(), request));
    }
}