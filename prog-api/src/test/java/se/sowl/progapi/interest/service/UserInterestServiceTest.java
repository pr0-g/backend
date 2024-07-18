package se.sowl.progapi.interest.service;


import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.user.exception.UserNotExistException;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.domain.UserInterest;
import se.sowl.progdomain.interest.repository.InterestRepository;
import se.sowl.progdomain.interest.repository.UserInterestRepository;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserInterestServiceTest {

    @Autowired
    private  UserInterestService userInterestService;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private UserInterestRepository userInterestRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
        interestRepository.deleteAllInBatch();
        userInterestRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유저가 없는 경우 UserNotExist 예외를 응답 해야 한다.")
    void userNotExist() {
        // given

        // when & then
        assertThrows(UserNotExistException.class, () -> {
            userInterestService.updateUserInterests(1L, List.of(1L, 2L));
        });
    }

    @Test
    @DisplayName("유저 관심사 정보가 이미 있는 경우, 모두 삭제 후 새로운 관심사를 추가 해야한다.")
    void addAndDeleteUserInterestWhenAlreadyExist() {
        // given
        User user = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.ckm0", "naver");
        userRepository.save(user);

        Interest interest1 = new Interest("SF");
        Interest interest2 = new Interest("판타지");
        Interest interest3 = new Interest("로맨스");
        interestRepository.saveAll(List.of(interest1, interest2, interest3));

        UserInterest item1 = new UserInterest(user, interest1);
        UserInterest item2 = new UserInterest(user, interest2);
        UserInterest item3 = new UserInterest(user, interest3);
        userInterestRepository.saveAll(List.of(
                item1,
                item2,
                item3
        ));

        // when
        userInterestService.updateUserInterests(
                user.getId(),
                List.of(interest1.getId(), interest3.getId())
        );
        List<UserInterest> result = userInterestRepository.findAllByUserId(user.getId()).get();
        // then
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("유저 관심사 정보가 없는 경우, 새로운 관심사를 추가 해야한다.")
    void addUserInterest() {
        // given
        User user = UserFixture.createUser(2L, "안녕", "안녕1", "dasd@naver.ckm0", "naver");
        userRepository.save(user);

        Interest interest1 = new Interest("SF");
        Interest interest2 = new Interest("판타지");
        Interest interest3 = new Interest("로맨스");
        interestRepository.saveAll(List.of(interest1, interest2, interest3));

        // when
        userInterestService.updateUserInterests(
                2L,
                List.of(interest1.getId(),interest2.getId(), interest3.getId())
        );
        List<UserInterest> result = userInterestRepository.findAllByUserId(user.getId()).get();
        // then
        assertThat(result.size()).isEqualTo(3);
    }
}