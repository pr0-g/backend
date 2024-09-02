package se.sowl.progapi.user.controller;
import org.junit.jupiter.api.BeforeEach;
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
import se.sowl.progapi.interest.service.UserInterestService;
import se.sowl.progapi.oauth.service.OAuthService;
import se.sowl.progapi.user.dto.UserInfoRequest;
import se.sowl.progapi.user.exception.UserException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @MockBean
    private UserInterestService userInterestService;

    private User testUser;
    private CustomOAuth2User customOAuth2User;

    @BeforeEach
    void setUp() {
        testUser = UserFixture.createUser(1L, "테스트", "테스트유저", "test@example.com", "naver");
        customOAuth2User = UserFixture.createCustomOAuth2User(testUser);
        when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
    }


    @Nested
    @DisplayName("GET /api/users/me")
    class GetMe {
        @Test
        @DisplayName("인증된 사용자의 정보를 조회할 수 있다")
        @WithMockUser(roles = "USER")
        public void getMeSuccess() throws Exception {
            // given
            List<UserInterestRequest> interests = Arrays.asList(
                    new UserInterestRequest(1L, "Technology"),
                    new UserInterestRequest(2L, "Travel")
            );

            UserInfoRequest userInfoRequest = new UserInfoRequest(
                    testUser.getId(), "test@example.com", "Test User", "testuser", "google", interests
            );

            when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
            when(userService.getUserInfo(eq(testUser.getId()))).thenReturn(userInfoRequest);

            // when & then
            mockMvc.perform(get("/api/users/me")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.result.userId").value(testUser.getId()))
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

            // given
            doThrow(new InvalidNicknameException("닉네임은 2자 이상 15자 이하여야 합니다.")).when(userService).editUser(eq(testUser.getId()), any());

            // when & then
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

    @Test
    @DisplayName("인증된 사용자의 관심사 목록을 조회할 수 있다")
    @WithMockUser(roles = "USER")
    public void getUserInterestsSuccess() throws Exception {
        // given
        List<UserInterestRequest> interests = Arrays.asList(
                new UserInterestRequest(1L, "Technology"),
                new UserInterestRequest(2L, "Travel")
        );

        when(userInterestService.getUserInterests(eq(testUser.getId()))).thenReturn(interests);

        // when & then
        mockMvc.perform(get("/api/users/interests")
                        .with(oauth2Login().oauth2User(customOAuth2User))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[0].name").value("Technology"))
                .andExpect(jsonPath("$.result[1].id").value(2))
                .andExpect(jsonPath("$.result[1].name").value("Travel"))
                .andDo(document("user-interests",
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("result").description("사용자 관심사 목록"),
                                fieldWithPath("result[].id").description("관심사 ID"),
                                fieldWithPath("result[].name").description("관심사 이름")
                        )
                ));
    }

    @Nested
    @DisplayName("GET /api/users/intrests")
    class GetUserInterests {
        @Test
        @DisplayName("인증된 사용자의 관심사 목록을 조회할 수 있다")
        @WithMockUser(roles = "USER")
        public void getUserInterestsSuccess() throws Exception {
            // given

            List<UserInterestRequest> interests = Arrays.asList(
                    new UserInterestRequest(1L, "Technology"),
                    new UserInterestRequest(2L, "Travel")
            );

            when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
            when(userInterestService.getUserInterests(eq(testUser.getId()))).thenReturn(interests);

            // when & then
            mockMvc.perform(get("/api/users/interests")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(2))
                    .andExpect(jsonPath("$.result[0].id").value(1))
                    .andExpect(jsonPath("$.result[0].name").value("Technology"))
                    .andExpect(jsonPath("$.result[1].id").value(2))
                    .andExpect(jsonPath("$.result[1].name").value("Travel"))
                    .andDo(document("user-interests",
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result").description("사용자 관심사 목록"),
                                    fieldWithPath("result[].id").description("관심사 ID"),
                                    fieldWithPath("result[].name").description("관심사 이름")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/withdraw")
    class WithdrawUser {
        @Test
        @DisplayName("인증된 사용자가 탈퇴할 수 있다")
        @WithMockUser(roles = "USER")
        void withdrawUserSuccess() throws Exception {
            // given
            Long userId = 1L;
            CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(
                    UserFixture.createUser(userId, "Test User", "testuser", "test@example.com", "google")
            );
            doNothing().when(userService).withdrawUser(userId);

            // when & then
            mockMvc.perform(delete("/api/users/withdraw")
                            .with(csrf())
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.result").doesNotExist())
                    .andDo(document("user-withdraw",
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result").description("결과 데이터 (탈퇴 시 null)")
                            )
                    ));

            verify(userService, times(1)).withdrawUser(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자가 탈퇴를 시도하면 실패한다")
        @WithMockUser(roles = "USER")
        void withdrawUserNotFound() throws Exception {
            // given
            Long userId = 999L;
            CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(
                    UserFixture.createUser(userId, "Test User", "testuser", "test@example.com", "google")
            );
            doThrow(new UserException.UserNotExistException()).when(userService).withdrawUser(userId);

            // when & then
            mockMvc.perform(delete("/api/users/withdraw")
                            .with(csrf())
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("FAIL"))
                    .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."))
                    .andExpect(jsonPath("$.result").doesNotExist())
                    .andDo(document("user-withdraw-not-found",
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result").description("결과 데이터 (실패 시 null)")
                            )
                    ));

            verify(userService, times(1)).withdrawUser(userId);
        }
    }
}
