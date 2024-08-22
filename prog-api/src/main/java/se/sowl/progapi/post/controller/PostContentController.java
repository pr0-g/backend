package se.sowl.progapi.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.post.dto.EditPostRequest;
import se.sowl.progapi.post.dto.PostDetailRequest;
import se.sowl.progapi.post.dto.PostDetailResponse;
import se.sowl.progapi.post.service.PostService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostContentController {

    private final PostService postService;

    @PutMapping("/edit")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<PostDetailResponse> editPost(
            @AuthenticationPrincipal CustomOAuth2User user,
            @Valid @RequestBody EditPostRequest request
    ) {
        PostDetailResponse response = postService.editPost(user.getUserId(), request);
        return CommonResponse.ok(response);
    }

    @GetMapping("/detail")
    public CommonResponse<PostDetailResponse> getPostDetail(@Valid @RequestBody PostDetailRequest request) {
        PostDetailResponse response = postService.getPostDetail(request.getPostId());
        return CommonResponse.ok(response);
    }
}