package se.sowl.progapi.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progapi.post.dto.RecentPostResponse;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class RecentPostServiceTest {

    @Autowired
    private RecentPostService recentPostService;

    @Autowired
    private PostRepository recentPostRepository;

    @MockBean
    private LikeService likeService;

    @BeforeEach
    void setUp() {
        recentPostRepository.deleteAll();
        when(likeService.getLikeCount(anyLong())).thenReturn(0L);
    }

    private void createTestPosts(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Post post = Post.builder()
                    .title("Test Post " + i)
                    .userId((long) (i % 5 + 1))
                    .interestId((long) (i % 10 + 1))
                    .thumbnailUrl("http://example.com/thumbnail" + i + ".jpg")
                    .build();
            posts.add(post);
        }
        recentPostRepository.saveAll(posts);
    }

    @Test
    @DisplayName("최근 게시물을 페이지별로 조회할 수 있다")
    void getRecentPosts() {
        // given
        createTestPosts(50);
        when(likeService.getLikeCount(anyLong())).thenReturn(10L);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        RecentPostResponse result = recentPostService.getRecentPosts(pageRequest);

        // then
        assertThat(result.getPosts()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(50);
        assertThat(result.getTotalPages()).isEqualTo(5);

        assertThat(result.getPosts())
                .isSortedAccordingTo((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

        assertThat(result.getPosts()).allMatch(post ->
                post.getThumbnailUrl().startsWith("http://example.com/thumbnail") &&
                        post.getLikeCount() == 10L
        );
    }

    @Test
    @DisplayName("조회되는 게시물이 없는 경우 빈 배열을 반환한다")
    void getRecentPostsWhenNoPostsExist() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        RecentPostResponse result = recentPostService.getRecentPosts(pageRequest);

        // then
        assertThat(result.getPosts()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("게시물에 좋아요가 없는 경우 좋아요 수를 0으로 반환한다")
    void getRecentPostsWithZeroLikes() {
        // given
        createTestPosts(10);
        when(likeService.getLikeCount(anyLong())).thenReturn(0L);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        RecentPostResponse result = recentPostService.getRecentPosts(pageRequest);

        // then
        assertThat(result.getPosts()).hasSize(10);
        assertThat(result.getPosts()).allMatch(post -> post.getLikeCount() == 0L);
    }

    @Test
    @DisplayName("게시물에 좋아요가 있는 경우 좋아요 수를 반환한다")
    void getRecentPostsWithLikes() {
        // given
        createTestPosts(10);
        when(likeService.getLikeCount(anyLong())).thenReturn(10L);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        RecentPostResponse result = recentPostService.getRecentPosts(pageRequest);

        // then
        assertThat(result.getPosts()).hasSize(10);
        assertThat(result.getPosts()).allMatch(post -> post.getLikeCount() == 10L);
    }


}
