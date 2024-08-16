package se.sowl.progapi.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.post.dto.LikeRequest;
import se.sowl.progapi.post.service.LikeService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/post/like")
@RequiredArgsConstructor
public class PostLikeController {

    private final LikeService likeService;


    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<LikeRequest> toggleLike(@AuthenticationPrincipal CustomOAuth2User user, @Valid @RequestBody LikeRequest request) {
        boolean isLiked = likeService.toggleLike(request.getPostId(), user.getUserId());
        long likeCount = likeService.getLikeCount(request.getPostId());
        LikeRequest response = LikeRequest.createResponse(request.getPostId(), isLiked, likeCount);
        return CommonResponse.ok(response);
    }
}
