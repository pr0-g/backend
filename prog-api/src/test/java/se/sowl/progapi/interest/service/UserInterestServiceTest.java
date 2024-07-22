package se.sowl.progapi.interest.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
        userInterestRepository.deleteAllInBatch();
        interestRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유저가 없는 경우 UserNotExist 예외를 응답해야 한다.")
    void userNotExist() {
        // given
        // when & then
        assertThrows(UserNotExistException.class, () -> {
            userInterestService.updateUserInterests(1L, List.of(1L, 2L));
        });
    }

    @Test
    @DisplayName("존재하지 않는 관심사 ID로 업데이트 시도 시 IllegalArgumentException 예외가 발생해야 한다.")
    void updateUserInterestsWithNonExistentInterestId() {
        // given
        User fixtureUser = UserFixture.createUser(null, "테스트", "테스트닉네임", "test@example.com", "google");
        User user = userRepository.save(fixtureUser);

        List<Interest> interests = saveAllInterest(List.of("SF", "판타지"));
        Long nonExistentInterestId = 9999L;

        // when & then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userInterestService.updateUserInterests(
                user.getId(),
                List.of(interests.get(0).getId(), nonExistentInterestId)
            );
        });
        assertThat(exception.getMessage()).contains("존재하지 않는 관심사 ID가 포함되어 있습니다.");
        List<UserInterest> result = userInterestRepository.findAllByUserId(user.getId()).orElse(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("유저 관심사 정보가 이미 있는 경우, 모두 삭제 후 새로운 관심사를 추가 해야한다.")
    void addAndDeleteUserInterestWhenAlreadyExist() {
        // given
        User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
        User user = userRepository.save(fixtureUser);

        List<Interest> interests = saveAllInterest(List.of("SF", "판타지", "로맨스"));

        UserInterest item1 = new UserInterest(user, interests.get(0));
        UserInterest item2 = new UserInterest(user, interests.get(1));
        UserInterest item3 = new UserInterest(user, interests.get(2));
        userInterestRepository.saveAll(List.of(item1, item2, item3));

        // when
        userInterestService.updateUserInterests(
                user.getId(),
                List.of(interests.get(0).getId(), interests.get(2).getId())
        );
        List<UserInterest> result = userInterestRepository.findAllByUserId(user.getId()).get();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ui -> ui.getInterest().getId())
            .containsExactlyInAnyOrder(interests.get(0).getId(), interests.get(2).getId());
    }

    @Test
    @DisplayName("유저 관심사 정보가 없는 경우, 새로운 관심사를 추가 해야한다.")
    void addUserInterest() {
        // given
        User fixtureUser = UserFixture.createUser(3L, "안녕", "안녕1", "ggg@naver.com", "google");
        User user = userRepository.save(fixtureUser);

        List<Interest> interests = saveAllInterest(List.of("SF", "판타지", "로맨스"));

        // when
        userInterestService.updateUserInterests(
            user.getId(),
            List.of(interests.get(0).getId(), interests.get(1).getId(), interests.get(2).getId())
        );
        List<UserInterest> result = userInterestRepository.findAllByUserId(user.getId()).get();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(ui -> ui.getInterest().getId())
            .containsExactlyInAnyOrder(
                interests.get(0).getId(), interests.get(1).getId(), interests.get(2).getId()
            );
    }

    private List<Interest> saveAllInterest(List<String> names) {
        for(String name : names) {
            interestRepository.save(new Interest(name));
        }
        return interestRepository.findAll();
    }
}
