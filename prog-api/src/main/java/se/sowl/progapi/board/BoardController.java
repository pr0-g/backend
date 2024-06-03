package se.sowl.progapi.board;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;

@RestController
@RequestMapping("/boards")
public class BoardController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Long> getBoards(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        return CommonResponse.ok(oAuth2User.getId());
    }
}

