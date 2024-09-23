package se.sowl.progapi.fixture;

import org.springframework.test.util.ReflectionTestUtils;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.domain.PostContent;

import java.time.LocalDateTime;

public class PostFixture {
    public static Post createPost(Long id, String title, Long userId, Interest interest, String thumbnailUrl, String content) {
        if (interest == null) {
            throw new IllegalArgumentException("Interest must not be null");
        }
        Post post = Post.builder()
                .title(title)
                .userId(userId)
                .interest(interest)
                .thumbnailUrl(thumbnailUrl)
                .createdAt(LocalDateTime.now())
                .build();
        if (id != null) {
            ReflectionTestUtils.setField(post, "id", id);
        }
        PostContent postContent = new PostContent(post, content);
        post.setPostContent(postContent);
        return post;
    }

    public static Interest createInterest(Long id, String name) {
        Interest interest = Interest.builder()
                .name(name)
                .build();
        if (id != null) {
            ReflectionTestUtils.setField(interest, "id", id);
        }
        return interest;
    }
}