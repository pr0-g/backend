package se.sowl.progapi.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class RecentPostResponse {
    private List<PostSummary> posts;
    private long totalElements;
    private int totalPages;

    public static RecentPostResponse from(List<PostSummary> posts, long totalElements, int totalPages) {
        return RecentPostResponse.builder()
                .posts(posts)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}