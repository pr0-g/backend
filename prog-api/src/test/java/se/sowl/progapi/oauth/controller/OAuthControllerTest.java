package se.sowl.progapi.oauth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.progapi.user.dto.UserInfoRequest;
import se.sowl.progapi.user.service.UserService;
import se.sowl.progapi.interest.service.UserInterestService;
import se.sowl.progapi.interest.dto.UserInterestRequest;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OAuthController.class)
class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserInterestService userInterestService;

    @Test
    void testGetUserInfo() throws Exception {
        // 테스트 데이터 설정
        Long userId = 1L;
        String email = "test@example.com";
        String name = "Test User";
        String nickname = "tester";
        String provider = "google";

        // OAuth2User 객체 생성
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", userId.toString());
        attributes.put("email", email);
        attributes.put("name", name);
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        OAuth2User oAuth2User = new DefaultOAuth2User(authorities, attributes, "sub");

        // UserInfoRequest 객체 생성
        UserInfoRequest userInfoRequest = new UserInfoRequest(userId, email, name, nickname, provider, Collections.emptyList());
        when(userService.getUserInfo(userId)).thenReturn(userInfoRequest);

        // UserInterestRequest 리스트 생성
        List<UserInterestRequest> interests = Arrays.asList(
                new UserInterestRequest(1L, "Interest 1"),
                new UserInterestRequest(2L, "Interest 2")
        );
        when(userInterestService.getUserInterests(userId)).thenReturn(interests);

        // 테스트 실행
        mockMvc.perform(get("/oauth2/login/info")
                        .with(oauth2Login().oauth2User(oAuth2User)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.result.isLoggedIn").value(true))
                .andExpect(jsonPath("$.result.user.id").value(userId))
                .andExpect(jsonPath("$.result.user.email").value(email))
                .andExpect(jsonPath("$.result.user.name").value(name))
                .andExpect(jsonPath("$.result.user.nickname").value(nickname))
                .andExpect(jsonPath("$.result.user.provider").value(provider))
                .andExpect(jsonPath("$.result.user.interests[0].id").value(1))
                .andExpect(jsonPath("$.result.user.interests[0].name").value("Interest 1"))
                .andExpect(jsonPath("$.result.user.interests[1].id").value(2))
                .andExpect(jsonPath("$.result.user.interests[1].name").value("Interest 2"));
    }
}