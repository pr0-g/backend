package se.sowl.progapi.interest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.interest.dto.UserInterestRequest;
import se.sowl.progapi.interest.service.InterestService;
import se.sowl.progapi.interest.service.UserInterestService;
import se.sowl.progapi.interest.dto.EditUserInterestRequest;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    private final UserInterestService userInterestService;

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<List<Interest>> getInterestList() {
        List<Interest> interestList = interestService.getList();
        return CommonResponse.ok(interestList);
    }

    @PutMapping("/user/edit")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> editUserInterests(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody EditUserInterestRequest request) {
        userInterestService.updateUserInterests(user.getUserId(), request.getInterestIdList());
        return CommonResponse.ok();
    }
}
