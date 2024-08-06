package se.sowl.progapi.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.sowl.progapi.common.CommonResponse;
import se.sowl.progapi.post.dto.PostSummary;
import se.sowl.progapi.post.service.LikedPostService;
import se.sowl.progapi.post.service.RecentPostService;
import se.sowl.progapi.post.service.TrendingPostService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final TrendingPostService trendingPostService;
    private final RecentPostService recentPostService;
    private final LikedPostService likedPostService;

    @GetMapping("/trending")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Page<PostSummary>> getTrendingPosts(@RequestParam("page") int page, @RequestParam("size") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostSummary> trendingPosts = trendingPostService.getTrendingPosts(pageable);
        return CommonResponse.ok(trendingPosts);
    }

    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Page<PostSummary>> getRecentPost(@RequestParam("page") int page, @RequestParam("size") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostSummary> trendingPosts = recentPostService.getRecentPosts(pageable);
        return CommonResponse.ok(trendingPosts);
    }

    @GetMapping("/liked")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Page<PostSummary>> getLikedPost(
        @AuthenticationPrincipal CustomOAuth2User user, @RequestParam("page") int page, @RequestParam("size") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostSummary> trendingPosts = likedPostService.getLikedPosts(user.getUserId(), pageable);
        return CommonResponse.ok(trendingPosts);
    }
}