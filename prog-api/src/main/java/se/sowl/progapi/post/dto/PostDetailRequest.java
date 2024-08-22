package se.sowl.progapi.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailRequest {

    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long postId;
}