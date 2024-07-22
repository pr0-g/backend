package se.sowl.progapi.interest.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.progapi.user.exception.UserNotExistException;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.domain.UserInterest;
import se.sowl.progdomain.interest.repository.InterestRepository;
import se.sowl.progdomain.interest.repository.UserInterestRepository;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserInterestService {

    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;

    @Transactional
    public void updateUserInterests(Long userId, List<Long> interestIdList){
        userInterestRepository.deleteAllByUserId(userId);
        User user = userRepository.findById(userId).orElseThrow(UserNotExistException::new);
        List<UserInterest> userInterests = getUserInterests(interestIdList, user);
        userInterestRepository.saveAll(userInterests);
    }

    private List<UserInterest> getUserInterests(List<Long> interestIdList, User user) {
        List<Interest> interests = interestRepository.findAllById(interestIdList);
        if (interests.size() != interestIdList.size()) {
            throw new IllegalArgumentException("존재하지 않는 관심사 ID가 포함되어 있습니다.");
        }
        return interests.stream().map(interest -> new UserInterest(user, interest)).toList();
    }

}
