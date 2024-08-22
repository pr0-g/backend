package se.sowl.progapi.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.oauth.service.OAuthService;
import se.sowl.progapi.post.dto.EditPostRequest;
import se.sowl.progapi.post.dto.PostDetailResponse;
import se.sowl.progapi.post.service.PostService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;
import se.sowl.progdomain.user.domain.User;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostContentController.class)
@AutoConfigureRestDocs
class PostContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OAuthService oAuthService;

    private User testUser;
    private CustomOAuth2User customOAuth2User;

    @BeforeEach
    void setUp() {
        testUser = UserFixture.createUser(1L, "테스트", "테스트유저", "test@example.com", "naver");
        customOAuth2User = UserFixture.createCustomOAuth2User(testUser);
        when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);
    }

    @Nested
    @DisplayName("PUT /api/posts/edit")
    class eidtPost {
        @Test
        @DisplayName("새 게시글 등록 성공")
        @WithMockUser(roles = "USER")
        void createNewPostSuccess() throws Exception {
            // Given
            EditPostRequest request = new EditPostRequest(null, "New Title", "New Content", 2L, "new_thumbnail.jpg");
            PostDetailResponse response = PostDetailResponse.builder()
                    .id(1L)
                    .title("New Title")
                    .userId(testUser.getId())
                    .interestId(2L)
                    .thumbnailUrl("new_thumbnail.jpg")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .content("New Content")
                    .likeCount(0L)
                    .build();
            when(postService.editPost(eq(testUser.getId()), any(EditPostRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(put("/api/posts/edit")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.id").value(1))
                    .andExpect(jsonPath("$.result.title").value("New Title"))
                    .andDo(document("create-new-post",
                            requestFields(
                                    fieldWithPath("id").description("새 게시글 등록 시 null").optional(),
                                    fieldWithPath("title").description("게시글 제목"),
                                    fieldWithPath("content").description("게시글 내용"),
                                    fieldWithPath("interestId").description("관심사 ID"),
                                    fieldWithPath("thumbnailUrl").description("썸네일 URL").optional()
                            ),
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result.id").description("생성된 게시글 ID"),
                                    fieldWithPath("result.title").description("게시글 제목"),
                                    fieldWithPath("result.userId").description("작성자 ID"),
                                    fieldWithPath("result.interestId").description("관심사 ID"),
                                    fieldWithPath("result.thumbnailUrl").description("썸네일 URL"),
                                    fieldWithPath("result.createdAt").description("생성 시간"),
                                    fieldWithPath("result.updatedAt").description("수정 시간"),
                                    fieldWithPath("result.content").description("게시글 내용"),
                                    fieldWithPath("result.likeCount").description("좋아요 수")
                            )));
        }

        @Test
        @DisplayName("기존 게시글 수정 성공")
        @WithMockUser(roles = "USER")
        void updateExistingPostSuccess() throws Exception {
            // Given
            Long postId = 1L;
            EditPostRequest request = new EditPostRequest(postId, "Updated Title", "Updated Content", 2L, "updated_thumbnail.jpg");
            PostDetailResponse response = PostDetailResponse.builder()
                    .id(postId)
                    .title("Updated Title")
                    .userId(testUser.getId())
                    .interestId(2L)
                    .thumbnailUrl("updated_thumbnail.jpg")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .content("Updated Content")
                    .likeCount(10L)
                    .build();
            when(postService.editPost(eq(testUser.getId()), any(EditPostRequest.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(put("/api/posts/edit")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.id").value(postId))
                    .andExpect(jsonPath("$.result.title").value("Updated Title"))
                    .andDo(document("update-existing-post",
                            requestFields(
                                    fieldWithPath("id").description("수정할 게시글 ID"),
                                    fieldWithPath("title").description("수정할 게시글 제목"),
                                    fieldWithPath("content").description("수정할 게시글 내용"),
                                    fieldWithPath("interestId").description("수정할 관심사 ID"),
                                    fieldWithPath("thumbnailUrl").description("수정할 썸네일 URL").optional()
                            ),
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result.id").description("수정된 게시글 ID"),
                                    fieldWithPath("result.title").description("수정된 게시글 제목"),
                                    fieldWithPath("result.userId").description("작성자 ID"),
                                    fieldWithPath("result.interestId").description("관심사 ID"),
                                    fieldWithPath("result.thumbnailUrl").description("썸네일 URL"),
                                    fieldWithPath("result.createdAt").description("생성 시간"),
                                    fieldWithPath("result.updatedAt").description("수정 시간"),
                                    fieldWithPath("result.content").description("게시글 내용"),
                                    fieldWithPath("result.likeCount").description("좋아요 수")
                            )));
        }
    }


    @Test
    @DisplayName("게시글 상세 조회 성공")
    @WithMockUser
    void getPostDetailSuccess() throws Exception {
        // Given
        Long postId = 1L;
        PostDetailResponse response = PostDetailResponse.builder()
                .id(postId)
                .title("Test Title")
                .userId(1L)
                .interestId(1L)
                .thumbnailUrl("test.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .content("Test Content")
                .likeCount(10L)
                .build();

        when(postService.getPostDetail(anyLong())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/posts/detail")
                        .content("{\"postId\": " + postId + "}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.result.id").value(postId))
                .andExpect(jsonPath("$.result.title").value("Test Title"))
                .andDo(document("get-post-detail",
                        requestFields(
                                fieldWithPath("postId").description("게시글 ID")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("result.id").description("게시글 ID"),
                                fieldWithPath("result.title").description("게시글 제목"),
                                fieldWithPath("result.userId").description("작성자 ID"),
                                fieldWithPath("result.interestId").description("관심사 ID"),
                                fieldWithPath("result.thumbnailUrl").description("썸네일 URL"),
                                fieldWithPath("result.createdAt").description("생성 시간"),
                                fieldWithPath("result.updatedAt").description("수정 시간"),
                                fieldWithPath("result.content").description("게시글 내용"),
                                fieldWithPath("result.likeCount").description("좋아요 수")
                        )));
    }
}