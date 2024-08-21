package se.sowl.progapi.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EditPostRequest {
    private Long id;

    @NotBlank(message = "게시글 제목은 비어있을 수 없습니다.")
    private String title;

    @NotBlank(message = "게시글 내용은 비어있을 수 없습니다.")
    private String content;

    @NotNull(message = "관심사 ID는 필수입니다.")
    private Long interestId;

    private String thumbnailUrl;
}