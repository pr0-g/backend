package se.sowl.progapi.board;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.progapi.common.CommonResponse;

@RestController
@RequestMapping("/boards")
public class BoardController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Long> getBoards(@AuthenticationPrincipal OAuth2User oAuth2User) {
        System.out.println(oAuth2User);
        return CommonResponse.ok();
    }
}

