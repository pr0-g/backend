package se.sowl.progapi.user.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.user.request.EditUserRequest;
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
    @DisplayName("유저의 2자 이상, 15자 이하의 닉네임이 들어오면 유저 정보를 갱신한다.")
    void setUserNickname() {

        // given
        User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
        User user = userRepository.save(fixtureUser);
        String newNickname = "안녕2";
        EditUserRequest editUserRequest = new EditUserRequest(newNickname);

        // when & then
        userService.editUser(user.getId(), editUserRequest);

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("유저의 새로운 닉네임이 15자 이상인 경우 에러를 표출해야 한다.")
    void setUserNicknameErr() {

        // given
        User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
        User user = userRepository.save(fixtureUser);
        String newNickname = "매우매우긴닉네임인데감당이가능할까요과연이게진짜과연";
        EditUserRequest editUserRequest = new EditUserRequest(newNickname);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.editUser(user.getId(), editUserRequest);
        });

    }
}
