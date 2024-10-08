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
    private PostRepository postRepository;

    @Autowired
    private InterestRepository interestRepository;

    @MockBean
    private LikeService likeService;

    private List<Interest> interests;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        interestRepository.deleteAll();
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

    private void createTestPosts(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Interest interest = interests.get(i % interests.size());
            Post post = Post.builder()
                    .title("Test Post " + i)
                    .userId((long) (i % 5 + 1))
                    .interest(interest)
                    .thumbnailUrl("http://example.com/thumbnail" + i + ".jpg")
                    .build();
            posts.add(post);
        }
        postRepository.saveAll(posts);
    }

    @Test
    @DisplayName("최근 게시물을 페이지별로 조회할 수 있다")
    void getRecentPosts() {
        // given
        createTestPosts(50);
        when(likeService.getLikeCount(anyLong())).thenReturn(10L);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<PostResponse> result = recentPostService.getRecentPosts(pageRequest);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(50);
        assertThat(result.getTotalPages()).isEqualTo(5);

        assertThat(result.getContent())
                .isSortedAccordingTo((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

        assertThat(result.getContent()).allMatch(post ->
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
        Page<PostResponse> result = recentPostService.getRecentPosts(pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
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
        Page<PostResponse> result = recentPostService.getRecentPosts(pageRequest);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getContent()).allMatch(post -> post.getLikeCount() == 0L);
    }

    @Test
    @DisplayName("게시물에 좋아요가 있는 경우 좋아요 수를 반환한다")
    void getRecentPostsWithLikes() {
        // given
        createTestPosts(10);
        when(likeService.getLikeCount(anyLong())).thenReturn(10L);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<PostResponse> result = recentPostService.getRecentPosts(pageRequest);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getContent()).allMatch(post -> post.getLikeCount() == 10L);
    }
}