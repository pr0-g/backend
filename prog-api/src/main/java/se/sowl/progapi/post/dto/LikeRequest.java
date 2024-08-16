package se.sowl.progapi.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeRequest {
    @NotNull(message = "게시물 ID는 필수입니다.")
    private Long postId;

    private boolean liked;
    private long likeCount;

    public LikeRequest(Long postId) {
        this.postId = postId;
    }

    public static LikeRequest createResponse(Long postId, boolean liked, long likeCount) {
        return new LikeRequest(postId, liked, likeCount);
    }
}