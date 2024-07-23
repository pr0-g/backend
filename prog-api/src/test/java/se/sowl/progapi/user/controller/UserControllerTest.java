package se.sowl.progapi.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    @Autowired
    private UserController userController;

    @Test
    @DisplayName("수정 요청")
    void editUserCorrectly() {
        // /api/users 요청을 보내고 응답을 확인한다.
        // 코드 작성

    }

}
