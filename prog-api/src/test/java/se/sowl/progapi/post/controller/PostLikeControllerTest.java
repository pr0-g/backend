package se.sowl.progapi.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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
import se.sowl.progapi.oauth.service.OAuthService;
import se.sowl.progapi.post.dto.LikeRequest;
import se.sowl.progapi.post.service.LikeService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;
import se.sowl.progdomain.user.domain.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostLikeController.class)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
class PostLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LikeService likeService;

    @MockBean
    private OAuthService oAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private CustomOAuth2User customOAuth2User;
    private static final Long TEST_POST_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = UserFixture.createUser(1L, "테스트", "테스트유저", "test@example.com", "naver");
        customOAuth2User = UserFixture.createCustomOAuth2User(testUser);
        when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
    }

    @Nested
    @DisplayName("PUT /api/posts/like")
    class ToggleLike {

        @Test
        @DisplayName("좋아요 토글 성공")
        @WithMockUser(roles = "USER")
        void toggleLikeSuccess() throws Exception {
            when(likeService.toggleLike(eq(TEST_POST_ID), eq(testUser.getId()))).thenReturn(true);
            when(likeService.getLikeCount(eq(TEST_POST_ID))).thenReturn(1L);

            LikeRequest request = new LikeRequest(TEST_POST_ID);
            String content = objectMapper.writeValueAsString(request);

            mockMvc.perform(put("/api/posts/like")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.postId").value(TEST_POST_ID))
                    .andExpect(jsonPath("$.result.liked").value(true))
                    .andExpect(jsonPath("$.result.likeCount").value(1))
                    .andDo(document("posts-like-toggle",
                            requestFields(
                                    fieldWithPath("postId").description("좋아요를 토글할 게시물 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result.postId").description("게시물 ID"),
                                    fieldWithPath("result.liked").description("좋아요 상태"),
                                    fieldWithPath("result.likeCount").description("총 좋아요 수")
                            )));
        }

        @Test
        @DisplayName("존재하지 않는 게시물에 대한 좋아요 토글 실패")
        @WithMockUser(roles = "USER")
        void toggleLikeNonExistentPost() throws Exception {
            when(likeService.toggleLike(eq(TEST_POST_ID), eq(testUser.getId())))
                    .thenThrow(new EntityNotFoundException("존재하지 않는 게시물입니다."));

            LikeRequest request = new LikeRequest(TEST_POST_ID);
            String content = objectMapper.writeValueAsString(request);

            mockMvc.perform(put("/api/posts/like")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("FAIL"))
                    .andExpect(jsonPath("$.message").value("존재하지 않는 게시물입니다."))
                    .andDo(document("posts-like-non-existent-post",
                            requestFields(
                                    fieldWithPath("postId").description("존재하지 않는 게시물 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result").description("결과 (오류 시 null)").optional()
                            )));
        }
    }
}