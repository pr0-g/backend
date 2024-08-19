package se.sowl.progapi.oauth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.interest.dto.UserInterestRequest;
import se.sowl.progapi.interest.service.UserInterestService;
import se.sowl.progapi.user.dto.UserInfoRequest;
import se.sowl.progapi.user.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class OAuthController {

    private final UserService userService;
    private final UserInterestService userInterestService;

    public OAuthController(UserService userService, UserInterestService userInterestService) {
        this.userService = userService;
        this.userInterestService = userInterestService;
    }

    @GetMapping("/login/info")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Map<String, Object>> getMe(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return CommonResponse.ok(Map.of("isLoggedIn", false));
        }
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("isLoggedIn", true);

        Long userId = Long.parseLong(user.getAttribute("sub"));
        UserInfoRequest userInfoRequest = userService.getUserInfo(userId);
        List<UserInterestRequest> interests = userInterestService.getUserInterests(userId);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userInfoRequest.getUserId());
        userInfo.put("email", userInfoRequest.getEmail());
        userInfo.put("name", userInfoRequest.getName());
        userInfo.put("nickname", userInfoRequest.getNickname());
        userInfo.put("provider", userInfoRequest.getProvider());
        userInfo.put("interests", interests);

        sessionInfo.put("user", userInfo);

        return CommonResponse.ok(sessionInfo);
    }
}
