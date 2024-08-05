package se.sowl.progapi.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import se.sowl.progapi.post.dto.PostSummary;
import se.sowl.progdomain.post.domain.Post;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final LikeService likeService;

    public Page<PostSummary> toPagePostSummary(Page<Post> pages) {
        List<PostSummary> list = pages.getContent().stream()
            .map(post -> {
                long likeCount = likeService.getLikeCount(post.getId());
                return PostSummary.from(post, likeCount);
            }).toList();
        return new PageImpl<>(list, PageRequest.of(pages.getNumber(), pages.getSize()), pages.getTotalElements());
    }
}
