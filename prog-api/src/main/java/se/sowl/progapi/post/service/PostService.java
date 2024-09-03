package se.sowl.progapi.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progapi.post.dto.EditPostRequest;
import se.sowl.progapi.post.dto.PostResponse;
import se.sowl.progapi.post.dto.PostDetailResponse;
import se.sowl.progapi.post.exception.PostException;
import se.sowl.progdomain.post.domain.Post;
import se.sowl.progdomain.post.domain.PostContent;
import se.sowl.progdomain.post.repository.PostContentRepository;
import se.sowl.progdomain.post.repository.PostRepository;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostContentRepository postContentRepository;
    private final LikeService likeService;

    @Transactional
    public PostDetailResponse editPost(Long userId, EditPostRequest request) {
        Post post;
        PostContent postContent;

        if (request.getId() == null) {
            post = createNewPost(userId, request);
            postContent = createNewPostContent(post.getId(), request.getContent());
        } else {
            post = updateExistingPost(userId, request);
            postContent = updateExistingPostContent(post.getId(), request.getContent());
        }

        return createPostDetailResponse(userId, post, postContent);
    }

    private Post createNewPost(Long userId, EditPostRequest request) {
        Post newPost = Post.builder()
                .title(request.getTitle())
                .userId(userId)
                .interestId(request.getInterestId())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();
        return postRepository.save(newPost);
    }

    private Post updateExistingPost(Long userId, EditPostRequest request) {
        Post existingPost = findPostById(request.getId());
        validatePostOwnership(existingPost, userId);
        existingPost.update(request.getTitle(), request.getInterestId(), request.getThumbnailUrl());
        return postRepository.save(existingPost);
    }

    private PostContent createNewPostContent(Long postId, String content) {
        PostContent newContent = new PostContent(postId, content);
        return postContentRepository.save(newContent);
    }

    private PostContent updateExistingPostContent(Long postId, String newContent) {
        PostContent existingContent = postContentRepository.findByPostId(postId)
                .orElseThrow(PostException.PostContentNotExistException::new);
        existingContent.updateContent(newContent);
        return postContentRepository.save(existingContent);
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
        PostContent postContent = postContentRepository.findByPostId(postId)
                .orElseThrow(PostException.PostContentNotExistException::new);

        return createPostDetailResponse(userId, post, postContent);
    }

    private PostDetailResponse createPostDetailResponse(Long userId, Post post, PostContent postContent) {
        Long writerId = post.getUserId();
        String writerNickname = userRepository.findById(writerId)
                .map(User::getNickname)
                .orElse("Unknown User");
        long likeCount = likeService.getLikeCount(post.getId());
        boolean userLiked = userLikePost(userId, post.getId());

        return PostDetailResponse.from(post, postContent, writerNickname, likeCount, userLiked);
    }

    public boolean existsPost(Long postId) {
        return postRepository.existsByIdAndDeletedFalse(postId);
    }

    private boolean userLikePost(Long userId, Long postId) {
        return likeService.hasUserLiked(userId, postId);
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