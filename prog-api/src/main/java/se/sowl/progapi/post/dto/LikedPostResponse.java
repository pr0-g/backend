package se.sowl.progapi.post.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikedPostResponse {
    private List<PostSummary> posts;
    private long totalElements;
    private int totalPages;

    public static LikedPostResponse from(List<PostSummary> posts, long totalElements, int totalPages) {
        return new LikedPostResponse(posts, totalElements, totalPages);
    }
}
