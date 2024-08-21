package se.sowl.progapi.post.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.oauth.service.OAuthService;
import se.sowl.progapi.post.dto.PostResponse;
import se.sowl.progapi.post.service.LikedPostService;
import se.sowl.progapi.post.service.RecentPostService;
import se.sowl.progapi.post.service.TrendingPostService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private TrendingPostService trendingPostService;

    @MockBean
    private RecentPostService recentPostService;

    @MockBean
    private LikedPostService likedPostService;

    @MockBean
    private UserRepository userRepository;

    private List<PostResponse> createPostSummaries() {
        return List.of(
                PostResponse.builder()
                        .id(1L)
                        .title("Post 1")
                        .thumbnailUrl("thumbnail1.jpg")
                        .createdAt(LocalDateTime.now().minusHours(1))
                        .likeCount(30L)
                        .build(),
                PostResponse.builder()
                        .id(2L)
                        .title("Post 2")
                        .thumbnailUrl("thumbnail2.jpg")
                        .createdAt(LocalDateTime.now().minusHours(2))
                        .likeCount(20L)
                        .build()
        );
    }

    @Nested
    @DisplayName("GET /api/posts/trending")
    class getTrendingPosts {
        @Test
        @DisplayName("사용자가 트렌딩 게시물을 페이지별로 조회할 수 있다")
        @WithMockUser
        void getTrendingPostsWithPagination() throws Exception {
            // given
            List<PostResponse> postSummaries = createPostSummaries();
            Page<PostResponse> page = new PageImpl<>(postSummaries, PageRequest.of(0, 10), 2);

            when(trendingPostService.getTrendingPosts(any())).thenReturn(page);

            // when & then
            mockMvc.perform(get("/api/posts/trending")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$.result.content.length()").value(2))
                    .andDo(document("get-trending-posts",
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result.content").description("트렌딩 게시물 목록"),
                                    fieldWithPath("result.content[].id").description("게시물 ID"),
                                    fieldWithPath("result.content[].title").description("게시물 제목"),
                                    fieldWithPath("result.content[].thumbnailUrl").description("썸네일 URL"),
                                    fieldWithPath("result.content[].createdAt").description("게시물 생성 시간"),
                                    fieldWithPath("result.content[].likeCount").description("좋아요 수"),
                                    fieldWithPath("result.pageable.pageNumber").description("현재 페이지 번호"),
                                    fieldWithPath("result.pageable.pageSize").description("페이지 크기"),
                                    fieldWithPath("result.pageable.sort.empty").description("정렬 정보 존재 여부"),
                                    fieldWithPath("result.pageable.sort.sorted").description("정렬 여부"),
                                    fieldWithPath("result.pageable.sort.unsorted").description("비정렬 여부"),
                                    fieldWithPath("result.pageable.offset").description("페이지 오프셋"),
                                    fieldWithPath("result.pageable.paged").description("페이징 사용 여부"),
                                    fieldWithPath("result.pageable.unpaged").description("페이징 미사용 여부"),
                                    fieldWithPath("result.totalPages").description("전체 페이지 수"),
                                    fieldWithPath("result.totalElements").description("전체 요소 수"),
                                    fieldWithPath("result.last").description("마지막 페이지 여부"),
                                    fieldWithPath("result.size").description("페이지 크기"),
                                    fieldWithPath("result.number").description("현재 페이지 번호"),
                                    fieldWithPath("result.sort.empty").description("정렬 정보 존재 여부"),
                                    fieldWithPath("result.sort.sorted").description("정렬 여부"),
                                    fieldWithPath("result.sort.unsorted").description("비정렬 여부"),
                                    fieldWithPath("result.first").description("첫 페이지 여부"),
                                    fieldWithPath("result.numberOfElements").description("현재 페이지의 요소 수"),
                                    fieldWithPath("result.empty").description("결과가 비어있는지 여부")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/recent")
    class getRecentPosts {
        @Test
        @DisplayName("사용자가 최근 게시물을 페이지별로 조회할 수 있다")
        @WithMockUser
        void getRecentPostsWithPagination() throws Exception {
            // given
            List<PostResponse> postSummaries = createPostSummaries();
            Page<PostResponse> page = new PageImpl<>(postSummaries, PageRequest.of(0, 10), 2);

            when(recentPostService.getRecentPosts(any())).thenReturn(page);

            // when & then
            mockMvc.perform(get("/api/posts/recent")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$.result.content.length()").value(2))
                    .andDo(document("get-recent-posts",
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result.content").description("최근 게시물 목록"),
                                    fieldWithPath("result.content[].id").description("게시물 ID"),
                                    fieldWithPath("result.content[].title").description("게시물 제목"),
                                    fieldWithPath("result.content[].thumbnailUrl").description("썸네일 URL"),
                                    fieldWithPath("result.content[].createdAt").description("게시물 생성 시간"),
                                    fieldWithPath("result.content[].likeCount").description("좋아요 수"),
                                    fieldWithPath("result.pageable.pageNumber").description("현재 페이지 번호"),
                                    fieldWithPath("result.pageable.pageSize").description("페이지 크기"),
                                    fieldWithPath("result.pageable.sort.empty").description("정렬 정보 존재 여부"),
                                    fieldWithPath("result.pageable.sort.sorted").description("정렬 여부"),
                                    fieldWithPath("result.pageable.sort.unsorted").description("비정렬 여부"),
                                    fieldWithPath("result.pageable.offset").description("페이지 오프셋"),
                                    fieldWithPath("result.pageable.paged").description("페이징 사용 여부"),
                                    fieldWithPath("result.pageable.unpaged").description("페이징 미사용 여부"),
                                    fieldWithPath("result.totalPages").description("전체 페이지 수"),
                                    fieldWithPath("result.totalElements").description("전체 요소 수"),
                                    fieldWithPath("result.last").description("마지막 페이지 여부"),
                                    fieldWithPath("result.size").description("페이지 크기"),
                                    fieldWithPath("result.number").description("현재 페이지 번호"),
                                    fieldWithPath("result.sort.empty").description("정렬 정보 존재 여부"),
                                    fieldWithPath("result.sort.sorted").description("정렬 여부"),
                                    fieldWithPath("result.sort.unsorted").description("비정렬 여부"),
                                    fieldWithPath("result.first").description("첫 페이지 여부"),
                                    fieldWithPath("result.numberOfElements").description("현재 페이지의 요소 수"),
                                    fieldWithPath("result.empty").description("결과가 비어있는지 여부")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/liked")
    class getLikedPosts {
        @Test
        @DisplayName("인증된 사용자가 좋아요한 게시물을 페이지별로 조회할 수 있다")
        @WithMockUser
        void getLikedPostsWithPagination() throws Exception {
            // given
            User fixtureUser = UserFixture.createUser(1L, "안녕", "안녕1", "dasd@naver.com", "naver");
            CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(fixtureUser);
            when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);

            List<PostResponse> postSummaries = createPostSummaries();
            Page<PostResponse> page = new PageImpl<>(postSummaries, PageRequest.of(0, 10), 2);
            when(likedPostService.getLikedPosts(anyLong(), any(Pageable.class))).thenReturn(page);

            // when & then
            mockMvc.perform(get("/api/posts/liked")
                            .with(oauth2Login().oauth2User(customOAuth2User))
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$.result.content.length()").value(2))
                    .andDo(document("get-liked-posts",
                            responseFields(
                                    fieldWithPath("code").description("응답 코드"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("result.content").description("좋아요한 게시물 목록"),
                                    fieldWithPath("result.content[].id").description("게시물 ID"),
                                    fieldWithPath("result.content[].title").description("게시물 제목"),
                                    fieldWithPath("result.content[].thumbnailUrl").description("썸네일 URL"),
                                    fieldWithPath("result.content[].createdAt").description("게시물 생성 시간"),
                                    fieldWithPath("result.content[].likeCount").description("좋아요 수"),
                                    fieldWithPath("result.pageable.pageNumber").description("현재 페이지 번호"),
                                    fieldWithPath("result.pageable.pageSize").description("페이지 크기"),
                                    fieldWithPath("result.pageable.sort.empty").description("정렬 정보 존재 여부"),
                                    fieldWithPath("result.pageable.sort.sorted").description("정렬 여부"),
                                    fieldWithPath("result.pageable.sort.unsorted").description("비정렬 여부"),
                                    fieldWithPath("result.pageable.offset").description("페이지 오프셋"),
                                    fieldWithPath("result.pageable.paged").description("페이징 사용 여부"),
                                    fieldWithPath("result.pageable.unpaged").description("페이징 미사용 여부"),
                                    fieldWithPath("result.totalPages").description("전체 페이지 수"),
                                    fieldWithPath("result.totalElements").description("전체 요소 수"),
                                    fieldWithPath("result.last").description("마지막 페이지 여부"),
                                    fieldWithPath("result.size").description("페이지 크기"),
                                    fieldWithPath("result.number").description("현재 페이지 번호"),
                                    fieldWithPath("result.sort.empty").description("정렬 정보 존재 여부"),
                                    fieldWithPath("result.sort.sorted").description("정렬 여부"),
                                    fieldWithPath("result.sort.unsorted").description("비정렬 여부"),
                                    fieldWithPath("result.first").description("첫 페이지 여부"),
                                    fieldWithPath("result.numberOfElements").description("현재 페이지의 요소 수"),
                                    fieldWithPath("result.empty").description("결과가 비어있는지 여부")
                            )
                    ));
        }
    }
}