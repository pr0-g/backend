package se.sowl.progapi.oauth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.oauth.service.OAuthService;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuthService oAuthService;

    @Test
    @WithMockUser
    void testGetUserInfo() throws Exception {
        CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(
            UserFixture.createUser(1L, "박정수", "test", "test@naver.com", "google")
        );
        when(oAuthService.loadUser(any())).thenReturn(customOAuth2User);

        mockMvc.perform(get("/oauth2/login/info")
                .with(oauth2Login().oauth2User(customOAuth2User)))
            .andExpect(status().isOk());
    }
}
