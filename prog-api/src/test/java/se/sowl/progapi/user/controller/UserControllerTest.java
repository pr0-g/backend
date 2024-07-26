package se.sowl.progapi.user.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.oauth.service.OAuthService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private OAuthService oAuthService;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }


    @Test
    @WithMockUser
    @Transactional
    @DisplayName("유저가 수정하고자 하는 닉네임의 길이가 2 보다 작은 경우, InvalidNicknameException 예외를 응답해야 한다.")
    void ifNewNicknameLengthSmallerThan2() throws Exception {
        User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
        User user = userRepository.save(fixtureUser);

        CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(user);
        when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/api/users/edit")
                .with(oauth2Login().oauth2User(customOAuth2User))
                .param("nickname", "a");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("FAIL"))
                .andExpect(jsonPath("$.message").value("닉네임은 2자 이상 15자 이하여야 합니다."))
                .andExpect(jsonPath("$.result").value(nullValue()));
    }

    @Test
    @WithMockUser
    @Transactional
    @DisplayName("유저가 수정하고자 하는 닉네임의 길이가 15 보다 큰 경우, InvalidNicknameException 예외를 응답해야 한다.")
    void ifNewNicknameLengthBiggerThan15() throws Exception {
        User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
        User user = userRepository.save(fixtureUser);

        CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(user);
        when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/api/users/edit")
                .with(oauth2Login().oauth2User(customOAuth2User))
                .param("nickname", "abcdefghijklmnop");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("FAIL"))
                .andExpect(jsonPath("$.message").value("닉네임은 2자 이상 15자 이하여야 합니다."))
                .andExpect(jsonPath("$.result").value(nullValue()));
    }

    @Test
    @WithMockUser
    @Transactional
    @DisplayName("유저가 수정하고자 하는 닉네임의 길이가 2 이상 15 이하인 경우, 유저 정보를 갱신해야 한다..")
    void ifNewNicknameValid() throws Exception {
        User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
        User user = userRepository.save(fixtureUser);

        CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(user);
        when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/api/users/edit")
                .with(oauth2Login().oauth2User(customOAuth2User))
                .param("nickname", "qwerty");

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.result").value(nullValue()));

        User result = userRepository.findById(user.getId()).get();
        assertThat(result.getNickname()).isEqualTo("qwerty");
    }

}
