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
        List<Interest> interests = interestRepository.findAllById(interestIdList);

        List<UserInterest> userInterests = interests.stream()
                .map(interest -> new UserInterest(user, interest))
                .toList();
        userInterestRepository.saveAll(userInterests);
    }

}
