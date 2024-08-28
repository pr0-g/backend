package se.sowl.progapi.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.interest.dto.UserInterestRequest;
import se.sowl.progapi.interest.service.UserInterestService;
import se.sowl.progapi.user.dto.EditUserRequest;
import se.sowl.progapi.user.dto.UserInfoRequest;
import se.sowl.progapi.user.service.UserService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserInterestService userInterestService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<UserInfoRequest> getMe(@AuthenticationPrincipal CustomOAuth2User user) {
        UserInfoRequest userInfo = userService.getUserInfo(user.getUserId());
        return CommonResponse.ok(userInfo);
    }

    @PutMapping("/edit")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> editUser(@AuthenticationPrincipal CustomOAuth2User user, @Valid @RequestBody EditUserRequest request) {
        userService.editUser(user.getUserId(), request);
        return CommonResponse.ok();
    }

    @GetMapping("/interests")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<List<UserInterestRequest>> getUserInterests(@AuthenticationPrincipal CustomOAuth2User user) {
        List<UserInterestRequest> userInterests = userInterestService.getUserInterests(user.getUserId());
        return CommonResponse.ok(userInterests);
    }

    // 유저 탈퇴 API
    @DeleteMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> withdrawUser(@AuthenticationPrincipal CustomOAuth2User user) {
        userService.withdrawUser(user.getUserId());
        return CommonResponse.ok();
    }

}
