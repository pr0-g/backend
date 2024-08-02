package se.sowl.progapi.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class EditUserRequest {
    @NotNull
    @Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하여야 합니다.")
    private final String nickname;

    public EditUserRequest(String nickname) {
        this.nickname = nickname;
    }
}
