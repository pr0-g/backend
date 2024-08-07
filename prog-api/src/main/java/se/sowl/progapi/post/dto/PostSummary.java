package se.sowl.progapi.post.dto;

import lombok.Builder;
import lombok.Getter;
import se.sowl.progdomain.post.domain.Post;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostSummary {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private long likeCount;

    public static PostSummary from(Post post, long likeCount) {
        return PostSummary.builder()
                .id(post.getId())
                .title(post.getTitle())
                .thumbnailUrl(post.getThumbnailUrl())
                .createdAt(post.getCreatedAt())
                .likeCount(likeCount)
                .build();
    }
}