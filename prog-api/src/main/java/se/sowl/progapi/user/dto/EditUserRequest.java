package se.sowl.progapi.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EditUserRequest {
    @NotNull
    @Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하여야 합니다.")
    private String nickname;
}
