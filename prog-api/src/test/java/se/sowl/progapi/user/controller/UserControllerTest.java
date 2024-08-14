package se.sowl.progapi.user.controller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.interest.dto.UserInterestRequest;
import se.sowl.progapi.oauth.service.OAuthService;
import se.sowl.progapi.user.dto.UserInfoRequest;
import se.sowl.progapi.user.service.UserService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;
import se.sowl.progdomain.user.InvalidNicknameException;
import se.sowl.progdomain.user.domain.User;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private UserService userService;


    @Nested
    @DisplayName("PUT /api/users/me")
    class GetMe {
        @Test
        @DisplayName("인증된 사용자의 정보를 조회할 수 있다")
        @WithMockUser(roles = "USER")
        public void getMeSuccess() throws Exception {
            // given
            Long userId = 1L;
            User fixtureUser = UserFixture.createUser(userId, "Test User", "testuser", "test@example.com", "google");
            CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(fixtureUser);

            List<UserInterestRequest> interests = Arrays.asList(
                    new UserInterestRequest(1L, "Technology"),
                    new UserInterestRequest(2L, "Travel")
            );

            UserInfoRequest userInfoRequest = new UserInfoRequest(
                    userId, "test@example.com", "Test User", "testuser", "google", interests
            );

            when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
            when(userService.getUserInfo(eq(userId))).thenReturn(userInfoRequest);

            // when & then
            mockMvc.perform(get("/api/users/me")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.result.userId").value(userId))
                    .andExpect(jsonPath("$.result.email").value("test@example.com"))
                    .andExpect(jsonPath("$.result.name").value("Test User"))
                    .andExpect(jsonPath("$.result.nickname").value("testuser"))
                    .andExpect(jsonPath("$.result.provider").value("google"))
                    .andExpect(jsonPath("$.result.interests[0].id").value(1))
                    .andExpect(jsonPath("$.result.interests[0].name").value("Technology"))
                    .andExpect(jsonPath("$.result.interests[1].id").value(2))
                    .andExpect(jsonPath("$.result.interests[1].name").value("Travel"))
                    .andDo(document("user-me",
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result.userId").description("사용자 ID"),
                                    fieldWithPath("result.email").description("사용자 이메일"),
                                    fieldWithPath("result.name").description("사용자 이름"),
                                    fieldWithPath("result.nickname").description("사용자 닉네임"),
                                    fieldWithPath("result.provider").description("인증 제공자"),
                                    fieldWithPath("result.interests[]").description("사용자 관심사 목록"),
                                    fieldWithPath("result.interests[].id").description("관심사 ID"),
                                    fieldWithPath("result.interests[].name").description("관심사 이름")
                            )
                    ));
        }
    }


    @Nested
    @DisplayName("PUT /api/users/edit")
    class EditUser {

        @Test
        @DisplayName("유저 정보 수정 성공")
        @WithMockUser(roles = "USER")
        public void editSuccess() throws Exception {
            User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
            CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(fixtureUser);
            when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
            doNothing().when(userService).editUser(eq(fixtureUser.getId()), any());

            mockMvc.perform(put("/api/users/edit")
                    .with(oauth2Login().oauth2User(customOAuth2User))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nickname\": \"qwerty\"}"))
                .andExpect(status().isOk())
                .andDo(document("user-edit",
                    requestFields(
                        fieldWithPath("nickname").description("수정할 닉네임")
                    ),
                    responseFields(
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("result").description("결과")
                    )));
        }

        @Test
        @DisplayName("유저 정보 수정 실패 (닉네임 길이 2미만 15이상)")
        @WithMockUser(roles = "USER")
        public void editFailByShortLength() throws Exception {
            User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
            CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(fixtureUser);
            when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
            doThrow(new InvalidNicknameException("닉네임은 2자 이상 15자 이하여야 합니다.")).when(userService).editUser(eq(fixtureUser.getId()), any());

            mockMvc.perform(put("/api/users/edit")
                    .with(oauth2Login().oauth2User(customOAuth2User))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nickname\": \"a\"}"))
                .andExpect(status().isBadRequest())
                .andDo(document("user-edit-fail",
                    requestFields(
                        fieldWithPath("nickname").description("수정할 닉네임")
                    ),
                    responseFields(
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("result").description("결과")
                    )));
        }
    }
}
