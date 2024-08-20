package se.sowl.progapi.post.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.post.dto.LikeRequest;
import se.sowl.progapi.post.dto.LikeResponse;
import se.sowl.progapi.post.service.LikeService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;

@RestController
@RequestMapping("/api/posts/like")
@RequiredArgsConstructor
public class PostLikeController {

    private final LikeService likeService;


    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<LikeResponse> toggleLike(
            @AuthenticationPrincipal CustomOAuth2User user,
            @Valid @RequestBody LikeRequest request
    ) {
        try {
            boolean isLiked = likeService.toggleLike(request.getPostId(), user.getUserId());
            long likeCount = likeService.getLikeCount(request.getPostId());
            LikeResponse response = LikeResponse.createResponse(request.getPostId(), isLiked, likeCount);
            return CommonResponse.ok(response);
        } catch (EntityNotFoundException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<Void> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return CommonResponse.fail(errorMessage);
    }
}
