package se.sowl.progapi.post.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.sowl.progapi.post.dto.PostDetailResponse;
import se.sowl.progapi.post.exception.PostException;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.domain.PostContent;
import se.sowl.progdomain.post.repository.PostContentRepository;
import se.sowl.progdomain.post.repository.PostRepository;
import se.sowl.progdomain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetPostDetailServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostContentRepository postContentRepository;

    @Mock
    private LikeService likeService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("존재하는 게시글의 상세 정보를 정확히 조회한다")
    void getExistingPostDetail() {
        // given
        Long postId = 1L;
        Post post = Post.builder()
                .title("Test Title")
                .userId(1L)
                .interestId(1L)
                .thumbnailUrl("test.jpg")
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(post, "id", postId);

        PostContent postContent = new PostContent(postId, "Test Content");
        long likeCount = 10L;

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postContentRepository.findByPostId(postId)).thenReturn(Optional.of(postContent));
        when(likeService.getLikeCount(postId)).thenReturn(likeCount);

        // when
        PostDetailResponse response = postService.getPostDetail(postId);

        // then
        assertNotNull(response);
        assertEquals(post.getTitle(), response.getTitle());
        assertEquals(postContent.getContent(), response.getContent());
        assertEquals(likeCount, response.getLikeCount());

        verify(postRepository).findById(postId);
        verify(postContentRepository).findByPostId(postId);
        verify(likeService).getLikeCount(postId);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외가 발생한다")
    void getNonExistentPostDetail() {
        // given
        Long nonExistentPostId = 999L;
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // when & then
        PostException.PostNotExistException exception = assertThrows(PostException.PostNotExistException.class,
                () -> postService.getPostDetail(nonExistentPostId));
        assertEquals("존재하지 않는 게시글입니다.", exception.getMessage());

        verify(postRepository).findById(nonExistentPostId);
        verifyNoInteractions(postContentRepository);
        verifyNoInteractions(likeService);
    }
}