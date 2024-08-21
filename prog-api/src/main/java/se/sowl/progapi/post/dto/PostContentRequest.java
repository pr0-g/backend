package se.sowl.progapi.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostContentRequest {
    @NotBlank(message = "게시글 내용은 비어있을 수 없습니다.")
    private String content;
}