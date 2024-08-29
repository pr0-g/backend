package se.sowl.progapi.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.sowl.progapi.interest.dto.UserInterestRequest;
import se.sowl.progapi.interest.service.UserInterestService;
import se.sowl.progapi.post.exception.PostException;
import se.sowl.progapi.user.dto.EditUserRequest;
import se.sowl.progapi.user.dto.UserInfoRequest;
import se.sowl.progapi.user.exception.UserException;
import se.sowl.progdomain.interest.repository.UserInterestRepository;
import se.sowl.progdomain.post.domain.PostContent;
import se.sowl.progdomain.post.repository.LikeRepository;
import se.sowl.progdomain.post.repository.PostRepository;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    private final UserInterestService userInterestService;

    public UserInfoRequest getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<UserInterestRequest> interests = userInterestService.getUserInterests(userId);

        return new UserInfoRequest(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getProvider(),
                interests
        );
    }

    public List<User> getList() {
        return userRepository.findAll();
    }

    @Transactional
    public void editUser(Long userId, EditUserRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        user.updateNickname(request.getNickname());
        userRepository.save(user);
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotExistException::new);

        user.anonymizePersonalData();
        postRepository.deleteAllByUserId(userId);
        userInterestRepository.deleteAllByUserId(userId);
        likeRepository.deleteAllByUserId(userId);

        userRepository.save(user);
    }

}
