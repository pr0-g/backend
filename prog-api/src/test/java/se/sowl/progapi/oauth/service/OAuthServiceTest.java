package se.sowl.progapi.oauth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OAuthServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthService oAuthService;

    @MockBean
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @Test
    @DisplayName("이미 가입 된 유저인 경우 갱신 후 유저 정보를 응답해야 한다.")
    @Transactional
    public void loadExistUser() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        User user = createUser(1L, "박정수", "화솔", "hwasowl598@gmail.com", "google");
        userRepository.save(user);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "dummy-access-token", null, null);
        ClientRegistration clientRegistration = createClientRegistration();
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        String updatedName = "박정수(수정됨)";
        Map<String, Object> attributes = Map.of("sub", "1234567890", "email", "hwasowl598@gmail.com", "name", updatedName);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        // when
        OAuth2User result = oAuthService.loadUser(userRequest);

        // then
        assertThat(result).isNotNull();
        Map<String, Object> resultAttributes = result.getAttributes();
        assertThat(resultAttributes.get("name")).isEqualTo(updatedName);
        assertThat(resultAttributes.get("email")).isEqualTo("hwasowl598@gmail.com");
        assertThat(resultAttributes.get("provider")).isEqualTo("google");
    }

    @Test
    @DisplayName("가입하지 않은 유저인 경우 저장 후 유저 정보를 응답해야 한다.")
    @Transactional
    public void loadNotExistUser() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "dummy-access-token", null, null);
        ClientRegistration clientRegistration = createClientRegistration();
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        Map<String, Object> attributes = Map.of("sub", "1234567890", "email", "hwasowl598@gmail.com", "name", "박정수");
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        // when
        OAuth2User result = oAuthService.loadUser(userRequest);

        // then
        assertThat(result).isNotNull();
        Map<String, Object> resultAttributes = result.getAttributes();
        assertThat(resultAttributes.get("name")).isEqualTo("박정수");
        assertThat(resultAttributes.get("email")).isEqualTo("hwasowl598@gmail.com");
        assertThat(resultAttributes.get("provider")).isEqualTo("google");
    }

    private User createUser(Long id, String name, String nickname, String email, String provider) {
        return User.builder()
            .id(id)
            .name(name)
            .nickname(nickname)
            .email(email)
            .provider(provider)
            .build();
    }

    private ClientRegistration createClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
            .clientId("clientId")
            .clientSecret("clientSecret")
            .scope("email")
            .authorizationUri("https://test/accounts.google.com/o/oauth2/auth")
            .tokenUri("https://test/oauth2.googleapis.com/token")
            .userInfoUri("https://test/www.googleapis.com/oauth2/v3/userinfo")
            .redirectUri("https://test/www.googleapis.com/oauth2/google/redirect")
            .userNameAttributeName("sub")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .clientName("Google")
            .build();
    }
}
