package se.sowl.progapi.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.interest.dto.UserInterestRequest;
import se.sowl.progapi.interest.service.UserInterestService;
import se.sowl.progapi.user.dto.EditUserRequest;
import se.sowl.progapi.user.dto.UserInfoRequest;
import se.sowl.progapi.user.exception.UserException;
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

    @DeleteMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> withdrawUser(@AuthenticationPrincipal CustomOAuth2User user,
                                             HttpServletRequest request) {
        try {
            userService.withdrawUser(user.getUserId());
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return CommonResponse.ok();

        } catch (UserException.UserNotExistException e) {
            return CommonResponse.fail("사용자를 찾을 수 없습니다.");

        } catch (Exception e) {
            return CommonResponse.fail("회원 탈퇴 중 오류가 발생했습니다.");
        }
    }

}
