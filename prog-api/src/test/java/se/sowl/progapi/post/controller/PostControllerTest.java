package se.sowl.progapi.post.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.progapi.post.dto.PostSummary;
import se.sowl.progapi.post.service.LikedPostService;
import se.sowl.progapi.post.service.RecentPostService;
import se.sowl.progapi.post.service.TrendingPostService;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrendingPostService trendingPostService;

    @MockBean
    private RecentPostService recentPostService;

    @MockBean
    private LikedPostService likedPostService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;


    @Nested
    @DisplayName("GET /api/posts/trending")
    class getTrendingPosts {
        @Test
        @DisplayName("사용자가 트렌딩 게시물을 페이지별로 조회할 수 있다")
        @WithMockUser
        void getTrendingPosts_ShouldReturnPagedTrendingPosts() throws Exception {
            // given
            List<PostSummary> postSummaries = List.of(
                    PostSummary.builder()
                            .id(1L)
                            .title("Title 1")
                            .thumbnailUrl("thumbnail1.jpg")
                            .createdAt(LocalDateTime.now())
                            .likeCount(50L)
                            .build(),
                    PostSummary.builder()
                            .id(2L)
                            .title("Title 2")
                            .thumbnailUrl("thumbnail2.jpg")
                            .createdAt(LocalDateTime.now())
                            .likeCount(100L)
                            .build()
            );
            Page<PostSummary> page = new PageImpl<>(postSummaries, PageRequest.of(0, 10), 2);

            when(trendingPostService.getTrendingPosts(any())).thenReturn(page);

            // when & then
            mockMvc.perform(get("/api/posts/trending")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("성공"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$.result.content.length()").value(2))
                    .andExpect(jsonPath("$.result.content[0].id").value(1))
                    .andExpect(jsonPath("$.result.content[0].title").value("Title 1"))
                    .andExpect(jsonPath("$.result.content[0].thumbnailUrl").value("thumbnail1.jpg"))
                    .andExpect(jsonPath("$.result.content[0].likeCount").value(50))
                    .andExpect(jsonPath("$.result.content[1].id").value(2))
                    .andExpect(jsonPath("$.result.content[1].title").value("Title 2"))
                    .andExpect(jsonPath("$.result.content[1].thumbnailUrl").value("thumbnail2.jpg"))
                    .andExpect(jsonPath("$.result.content[1].likeCount").value(100))
                    .andExpect(jsonPath("$.result.totalElements").value(2))
                    .andExpect(jsonPath("$.result.totalPages").value(1))
                    .andExpect(jsonPath("$.result.size").value(10))
                    .andExpect(jsonPath("$.result.number").value(0))
                    .andDo(print());
        }
    }


    @Nested
    @DisplayName("GET /api/posts/recent")
    class getRecentPosts {
        @Test
        @DisplayName("사용자가 최근 게시물을 페이지별로 조회할 수 있다")
        @WithMockUser
        void getRecentPosts_ShouldReturnPagedRecentPosts() throws Exception {
            // given
            List<PostSummary> postSummaries = List.of(
                    PostSummary.builder()
                            .id(1L)
                            .title("Recent Post 1")
                            .thumbnailUrl("thumbnail1.jpg")
                            .createdAt(LocalDateTime.now().minusHours(1))
                            .likeCount(30L)
                            .build(),
                    PostSummary.builder()
                            .id(2L)
                            .title("Recent Post 2")
                            .thumbnailUrl("thumbnail2.jpg")
                            .createdAt(LocalDateTime.now().minusHours(2))
                            .likeCount(20L)
                            .build()
            );
            Page<PostSummary> page = new PageImpl<>(postSummaries, PageRequest.of(0, 10), 2);

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
                    .andExpect(jsonPath("$.result.content[0].id").value(1))
                    .andExpect(jsonPath("$.result.content[0].title").value("Recent Post 1"))
                    .andExpect(jsonPath("$.result.content[0].thumbnailUrl").value("thumbnail1.jpg"))
                    .andExpect(jsonPath("$.result.content[0].likeCount").value(30))
                    .andExpect(jsonPath("$.result.content[1].id").value(2))
                    .andExpect(jsonPath("$.result.content[1].title").value("Recent Post 2"))
                    .andExpect(jsonPath("$.result.content[1].thumbnailUrl").value("thumbnail2.jpg"))
                    .andExpect(jsonPath("$.result.content[1].likeCount").value(20))
                    .andExpect(jsonPath("$.result.totalElements").value(2))
                    .andExpect(jsonPath("$.result.totalPages").value(1))
                    .andExpect(jsonPath("$.result.size").value(10))
                    .andExpect(jsonPath("$.result.number").value(0));
        }
    }
}