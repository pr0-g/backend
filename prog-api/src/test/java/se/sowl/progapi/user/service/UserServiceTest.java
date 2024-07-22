package se.sowl.progapi.user.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유저의 새로운 닉네임이 들어오면 닉네임을 업데이트한다.")
    void setUserNickname() {

        // given
        User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
        User user = userRepository.save(fixtureUser);
        String newNickname = "안녕2";

        // when & then
        userService.setUserNickname(user.getId(), newNickname);

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("유저의 새로운 닉네임이 15자 이상인 경우 에러를 표출해야 한다.")
    void setUserNicknameErr() {

        // given
        User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
        User user = userRepository.save(fixtureUser);
        String newNickname = "00000000000000000000000";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.setUserNickname(user.getId(), newNickname);
        });

    }
}