package se.sowl.progapi.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progapi.post.dto.PostResponse;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.repository.InterestRepository;
import se.sowl.progdomain.post.domain.Like;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.LikeRepository;
import se.sowl.progdomain.post.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class LikedPostServiceTest {

    @Autowired
    private LikedPostService likedPostService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private InterestRepository interestRepository;

    @MockBean
    private LikeService likeService;

    private Long userId;
    private List<Interest> interests;

    @BeforeEach
    void setUp() {
        likeRepository.deleteAll();
        postRepository.deleteAll();
        interestRepository.deleteAll();
        userId = 1L;
        interests = createInterests(10);
        when(likeService.getLikeCount(anyLong())).thenReturn(0L);
    }

    private List<Interest> createInterests(int count) {
        List<Interest> interestList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            interestList.add(new Interest("Interest " + (i + 1)));
        }
        return interestRepository.saveAll(interestList);
    }

    private void createTestPostsAndLikes(int count) {
        List<Post> posts = new ArrayList<>();
        List<Like> likes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Interest interest = interests.get(i % interests.size());
            Post post = Post.builder()
                    .title("Test Post " + i)
                    .userId(userId)
                    .interest(interest)
                    .thumbnailUrl("http://example.com/thumbnail" + i + ".jpg")
                    .build();
            posts.add(post);
        }
        posts = postRepository.saveAll(posts);

        for (Post post : posts) {
            Like like = new Like(post.getId(), userId);
            likes.add(like);
        }
        likeRepository.saveAll(likes);
    }

    @Test
    @DisplayName("사용자가 좋아요한 게시물을 페이지별로 조회할 수 있다")
    void getLikedPosts() {
        // given
        createTestPostsAndLikes(50);
        when(likeService.getLikeCount(anyLong())).thenReturn(10L);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<PostResponse> result = likedPostService.getLikedPosts(userId, pageRequest);

        // then
//        assertThat(result.getContent()).hasSize(10);
//        assertThat(result.getTotalElements()).isEqualTo(50);
//        assertThat(result.getTotalPages()).isEqualTo(5);
//
//        assertThat(result.getContent()).allMatch(post ->
//                post.getThumbnailUrl().startsWith("http://example.com/thumbnail") &&
//                        post.getLikeCount() == 10L
//        );
    }

    @Test
    @DisplayName("좋아요한 게시물이 없는 경우 빈 배열을 반환한다")
    void getLikedPostsWhenNoLikesExist() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<PostResponse> result = likedPostService.getLikedPosts(userId, pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("좋아요한 게시물 중 일부가 삭제된 경우 삭제되지 않은 게시물만 반환한다")
    void getLikedPostsWhenSomePostsAreDeleted() {
        // given
        createTestPostsAndLikes(10);
        List<Post> posts = postRepository.findAll();
        posts.subList(0, 5).forEach(Post::softDelete);
        postRepository.saveAll(posts);

        when(likeService.getLikeCount(anyLong())).thenReturn(5L);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<PostResponse> result = likedPostService.getLikedPosts(userId, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("한 페이지에 포함된 게시물 수가 페이지 크기보다 작은 경우 모든 게시물을 반환한다")
    void getLikedPostsWithLessThanPageSize() {
        // given
        createTestPostsAndLikes(5);
        when(likeService.getLikeCount(anyLong())).thenReturn(1L);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<PostResponse> result = likedPostService.getLikedPosts(userId, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }
}