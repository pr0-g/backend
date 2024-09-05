package se.sowl.progapi.post.dto;

import lombok.Builder;
import lombok.Getter;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.domain.PostContent;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostDetailResponse {
    private Long id;
    private String title;
    private Long writerId;
    private String writerNickname;
    private Interest interest;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String content;
    private long likeCount;
    private boolean userLiked;

    public static PostDetailResponse from(Post post, PostContent postContent, String writerNickname, Interest interest, long likeCount, boolean userLiked) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .writerId(post.getUserId())
                .writerNickname(writerNickname)
                .interest(interest)
                .thumbnailUrl(post.getThumbnailUrl())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .content(postContent.getContent())
                .likeCount(likeCount)
                .userLiked(userLiked)
                .build();
    }
}