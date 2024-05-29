package se.sowl.progapi.oauth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
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

    @SpyBean
    private OAuthService oAuthService;

    @Test
    @DisplayName("이미 가입 된 유저인 경우 갱신 후 유저 정보를 응답해야 한다.")
    @Transactional
    public void loadExistUser() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        User user = createUser(1L, "박정수", "화솔", "hwasowl598@gmail.com", "google");
        userRepository.save(user);

        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        String updatedName = "박정수(수정됨)";
        Map<String, Object> attributes = Map.of("email", "hwasowl598@gmail.com", "name", updatedName);
        when(userRequest.getClientRegistration()).thenReturn(createClientRegistration());
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        doReturn(oAuth2User).when(oAuthService).getOAuth2User(userRequest);

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

        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
        Map<String, Object> attributes = Map.of("email", "hwasowl598@gmail.com", "name", "박정수");
        when(userRequest.getClientRegistration()).thenReturn(createClientRegistration());
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        doReturn(oAuth2User).when(oAuthService).getOAuth2User(userRequest);

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
