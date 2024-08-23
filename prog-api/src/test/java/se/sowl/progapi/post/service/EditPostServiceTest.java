package se.sowl.progapi.post.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.sowl.progapi.post.dto.EditPostRequest;
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
public class EditPostServiceTest {

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
    @DisplayName("새 게시글을 성공적으로 생성한다")
    void createNewPost() {
        // given
        Long userId = 1L;
        Long postId = 1L; // 예상되는 새 게시글 ID
        EditPostRequest request = new EditPostRequest(null, "New Title", "New Content", 1L, "thumbnail.jpg");
        Post newPost = Post.builder()
                .title(request.getTitle())
                .userId(userId)
                .interestId(request.getInterestId())
                .thumbnailUrl(request.getThumbnailUrl())
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(newPost, "id", postId);

        PostContent newContent = new PostContent(postId, request.getContent());

        when(postRepository.save(any(Post.class))).thenReturn(newPost);
        when(postContentRepository.save(any(PostContent.class))).thenReturn(newContent);
        when(likeService.getLikeCount(postId)).thenReturn(0L);

        // when
        PostDetailResponse response = postService.editPost(userId, request);

        // then
        assertNotNull(response);
        assertEquals(newPost.getTitle(), response.getTitle());
        assertEquals(newContent.getContent(), response.getContent());
        assertEquals(0L, response.getLikeCount());

        verify(postRepository).save(any(Post.class));
        verify(postContentRepository).save(any(PostContent.class));
        verify(likeService).getLikeCount(postId);
    }

    @Test
    @DisplayName("기존 게시글을 성공적으로 수정한다")
    void updateExistingPost() {
        // given
        Long postId = 1L;
        Long userId = 1L;
        EditPostRequest request = new EditPostRequest(postId, "Updated Title", "Updated Content", 2L, "new_thumbnail.jpg");
        Post existingPost = Post.builder()
                .title("Original Title")
                .userId(userId)
                .interestId(1L)
                .thumbnailUrl("original_thumbnail.jpg")
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(existingPost, "id", postId);

        PostContent existingContent = new PostContent(postId, "Original Content");

        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(postContentRepository.findByPostId(postId)).thenReturn(Optional.of(existingContent));
        when(postRepository.save(any(Post.class))).thenReturn(existingPost);
        when(postContentRepository.save(any(PostContent.class))).thenReturn(existingContent);
        when(likeService.getLikeCount(postId)).thenReturn(5L);

        // when
        PostDetailResponse response = postService.editPost(userId, request);

        // then
        assertNotNull(response);
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getContent(), response.getContent());
        assertEquals(5L, response.getLikeCount());

        verify(postRepository).findById(postId);
        verify(postContentRepository).findByPostId(postId);
        verify(postRepository).save(any(Post.class));
        verify(postContentRepository).save(any(PostContent.class));
        verify(likeService).getLikeCount(postId);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 예외가 발생한다")
    void updateNonExistentPost() {
        // given
        Long postId = 999L;
        Long userId = 1L;
        EditPostRequest request = new EditPostRequest(postId, "Title", "Content", 1L, "thumbnail.jpg");

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(PostException.PostNotExistException.class, () -> postService.editPost(userId, request));

        verify(postRepository).findById(postId);
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(postContentRepository);
        verifyNoInteractions(likeService);
    }

    @Test
    @DisplayName("권한이 없는 사용자가 게시글 수정 시 예외가 발생한다")
    void updatePostWithoutAuthorization() {
        // given
        Long postId = 1L;
        Long userId = 1L;
        Long differentUserId = 2L;
        EditPostRequest request = new EditPostRequest(postId, "Title", "Content", 1L, "thumbnail.jpg");
        Post existingPost = Post.builder()
                .title("Original Title")
                .userId(differentUserId)
                .interestId(1L)
                .thumbnailUrl("original_thumbnail.jpg")
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(existingPost, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // when & then
        assertThrows(PostException.PostNotAuthorizedException.class, () -> postService.editPost(userId, request));

        verify(postRepository).findById(postId);
        verifyNoMoreInteractions(postRepository);
        verifyNoInteractions(postContentRepository);
        verifyNoInteractions(likeService);
    }
}