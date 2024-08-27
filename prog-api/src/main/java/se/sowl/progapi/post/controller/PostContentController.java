package se.sowl.progapi.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.post.dto.EditPostRequest;
import se.sowl.progapi.post.dto.PostDetailRequest;
import se.sowl.progapi.post.dto.PostDetailResponse;
import se.sowl.progapi.post.exception.PostException;
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
    public ResponseEntity<CommonResponse<PostDetailResponse>> editPost(
            @AuthenticationPrincipal CustomOAuth2User user,
            @Valid @RequestBody EditPostRequest request
    ) {
        try {
            PostDetailResponse response = postService.editPost(user.getUserId(), request);
            return ResponseEntity.ok(CommonResponse.ok(response));
        } catch (PostException e) {
            return new ResponseEntity<>(CommonResponse.fail(e.getMessage()), e.getStatus());
        }
    }

    @GetMapping("/detail")
    public CommonResponse<PostDetailResponse> getPostDetail(@RequestParam Long postId) {
        PostDetailResponse response = postService.getPostDetail(postId);
        return CommonResponse.ok(response);
    }

}