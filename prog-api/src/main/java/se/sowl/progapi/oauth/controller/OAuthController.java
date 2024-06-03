package se.sowl.progapi.oauth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.progapi.common.CommonResponse;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class OAuthController {

    // TODO: nav to main
    @GetMapping("/login/info")
    public CommonResponse<String> oauthLoginInfo(Authentication authentication){
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        return CommonResponse.ok(attributes.toString());
    }
}
