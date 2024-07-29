package se.sowl.progapi.interest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.interest.service.InterestService;
import se.sowl.progdomain.interest.domain.Interest;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<List<Interest>> getInterestList() {
        List<Interest> interestList = interestService.getList();
        return CommonResponse.ok(interestList);
    }
}
