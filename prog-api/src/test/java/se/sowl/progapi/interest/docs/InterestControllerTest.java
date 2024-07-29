package se.sowl.progapi.interest.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.interest.controller.InterestController;
import se.sowl.progapi.interest.request.EditUserInterestRequest;
import se.sowl.progapi.interest.service.InterestService;
import se.sowl.progapi.interest.service.UserInterestService;
import se.sowl.progapi.oauth.service.OAuthService;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.repository.InterestRepository;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterestController.class)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
public class InterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterestService interestService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private UserInterestService userInterestService;

    @MockBean
    private InterestRepository interestRepository;

    @BeforeEach
    void setUp() {
        interestRepository.deleteAll();

        interestRepository.saveAll(List.of(
                new Interest("SF"),
                new Interest("판타지"),
                new Interest("로맨스")
        ));
    }


    @Nested
    @DisplayName("PUT /api/interests/user/edit")
    class EditUserInterests {

        @Test
        @DisplayName("사용자 관심사 수정 성공")
        @WithMockUser(roles = "USER")
        public void editSuccess() throws Exception {
            User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
            CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(fixtureUser);
            when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
            doNothing().when(userInterestService).updateUserInterests(eq(fixtureUser.getId()), any());

            EditUserInterestRequest request = new EditUserInterestRequest(Arrays.asList(2L, 3L));
            String content = objectMapper.writeValueAsString(request);

            mockMvc.perform(put("/api/interests/user/edit")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andExpect(status().isOk())
                    .andDo(document("interest-edit",
                            requestFields(
                                    fieldWithPath("interestIdList").description("수정할 관심사 ID 목록")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result").description("결과").optional()
                            )));
        }

        @Test
        @DisplayName("사용자 관심사 수정 실패 (존재하지 않는 관심사 ID)")
        @WithMockUser(roles = "USER")
        public void editFailWithNonExistentId() throws Exception {
            User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
            CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(fixtureUser);
            when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
            doThrow(new IllegalArgumentException("존재하지 않는 관심사 ID가 포함되어 있습니다."))
                    .when(userInterestService).updateUserInterests(eq(fixtureUser.getId()), any());

            EditUserInterestRequest request = new EditUserInterestRequest(Arrays.asList(999L, 1000L));

            mockMvc.perform(put("/api/interests/user/edit")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andDo(document("interest-edit-fail",
                            requestFields(
                                    fieldWithPath("interestIdList").description("수정할 관심사 ID 목록")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result").description("결과").optional()
                            )));
        }
    }
}