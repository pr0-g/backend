package se.sowl.progapi.post.dto;

import lombok.Builder;
import lombok.Getter;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.domain.PostContent;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostDetailResponse {
    private Long id;
    private String title;
    private Long userId;
    private String userEmailId;
    private Long interestId;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String content;
    private long likeCount;

    public static PostDetailResponse from(Post post, PostContent postContent, String userEmailId,long likeCount) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .userId(post.getUserId())
                .userEmailId(userEmailId)
                .interestId(post.getInterestId())
                .thumbnailUrl(post.getThumbnailUrl())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .content(postContent.getContent())
                .likeCount(likeCount)
                .build();
    }
}