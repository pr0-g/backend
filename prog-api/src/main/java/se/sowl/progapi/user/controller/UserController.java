package se.sowl.progapi.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.user.request.SetUserNicknameRequest;
import se.sowl.progapi.user.service.UserService;
import se.sowl.progdomain.user.domain.CustomOAuth2User;
import se.sowl.progdomain.user.domain.User;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> getMe(@AuthenticationPrincipal CustomOAuth2User user) {
        List<User> users = userService.getList();
        System.out.println(users.toString());
        return CommonResponse.ok();
    }

    @PutMapping("/edit")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> setUserNickname(@AuthenticationPrincipal CustomOAuth2User user, @Valid SetUserNicknameRequest request) {
        userService.setUserNickname(user.getUserId(), request.getNickname());
        return CommonResponse.ok();
    }
}
