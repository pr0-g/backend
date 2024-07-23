package se.sowl.progapi.oauth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.progapi.common.CommonResponse;

@RestController
@RequestMapping("/oauth2")
public class OAuthController {

    @GetMapping("/login/info")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> getMe(@AuthenticationPrincipal OAuth2User user) {
        System.out.println(user.getAttributes());
        return CommonResponse.ok();
    }
}
