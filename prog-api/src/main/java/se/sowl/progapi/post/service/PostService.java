package se.sowl.progapi.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progapi.interest.service.InterestService;
import se.sowl.progapi.post.dto.EditPostRequest;
import se.sowl.progapi.post.dto.PostResponse;
import se.sowl.progapi.post.dto.PostDetailResponse;
import se.sowl.progapi.post.exception.PostException;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.repository.InterestRepository;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.domain.PostContent;
import se.sowl.progdomain.post.repository.PostRepository;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeService likeService;
    private final InterestService interestService;
    private final InterestRepository interestRepository;

    @Transactional
    public PostDetailResponse editPost(Long userId, EditPostRequest request) {
        Post post;

        if (request.getId() == null) {
            post = createNewPost(userId, request);
        } else {
            post = updateExistingPost(userId, request);
        }

        return createPostDetailResponse(userId, post);
    }

    private Post createNewPost(Long userId, EditPostRequest request) {
        Interest interest = interestRepository.findById(request.getInterestId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관심사입니다."));

        Post newPost = Post.builder()
                .title(request.getTitle())
                .userId(userId)
                .interest(interest)
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        PostContent newContent = new PostContent(newPost, request.getContent());
        newPost.setPostContent(newContent);

        return postRepository.save(newPost);
    }

    private Post updateExistingPost(Long userId, EditPostRequest request) {
        Post existingPost = findPostById(request.getId());

        Interest interest = interestRepository.findById(request.getInterestId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관심사입니다."));

        validatePostOwnership(existingPost, userId);
        existingPost.update(request.getTitle(), interest, request.getThumbnailUrl());

        PostContent postContent = existingPost.getPostContent();
        if (postContent == null) {
            postContent = new PostContent(existingPost, request.getContent());
            existingPost.setPostContent(postContent);
        } else {
            postContent.updateContent(request.getContent());
        }

        return postRepository.save(existingPost);
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(PostException.PostNotExistException::new);
    }

    private void validatePostOwnership(Post post, Long userId) {
        if (!post.getUserId().equals(userId)) {
            throw new PostException.PostNotAuthorizedException();
        }
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostException.PostNotExistException::new);
        return createPostDetailResponse(userId, post);
    }

    private PostDetailResponse createPostDetailResponse(Long userId, Post post) {

        Long writerId = post.getUserId();
        String writerNickname = getWriterNickname(writerId);
        Interest interest = post.getInterest();
        long likeCount = likeService.getLikeCount(post.getId());
        boolean userLiked = userLikePost(post.getId(), userId);

        return PostDetailResponse.from(post, post.getPostContent(), writerNickname, interest, likeCount, userLiked);
    }

    public boolean existsPost(Long postId) {
        return postRepository.existsByIdAndDeletedFalse(postId);
    }

    private String getWriterNickname(Long writerId) {
        return userRepository.findById(writerId)
                .map(user -> user.getNickname() != null ? user.getNickname() : user.getName())
                .orElse("탈퇴한 사용자");
    }

    private boolean userLikePost(Long postId, Long userId) {
        return likeService.hasUserLiked(postId, userId);
    }

    public Page<PostResponse> toPagePostResponse(Page<Post> pages) {
        List<PostResponse> list = pages.getContent().stream()
                .map(post -> {
                    long likeCount = likeService.getLikeCount(post.getId());

                    String writerId = String.valueOf(userRepository.findById(post.getUserId())
                            .map(User::getEmail)
                            .map(email -> email.split("@")[0]));

                    return PostResponse.from(post, writerId, likeCount);
                }).toList();
        return new PageImpl<>(list, PageRequest.of(pages.getNumber(), pages.getSize()), pages.getTotalElements());
    }

    public void softDeletePostByWithDrawUserId(Long userId) {
        List<Post> posts = postRepository.findAllByUserId(userId);
        posts.forEach(Post::softDelete);
        postRepository.saveAll(posts);
    }
}