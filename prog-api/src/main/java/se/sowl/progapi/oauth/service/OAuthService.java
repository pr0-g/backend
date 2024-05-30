package se.sowl.progapi.oauth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.progdomain.oauth.domain.OAuth2Extractor;
import se.sowl.progdomain.oauth.domain.OAuth2Attribute;
import se.sowl.progdomain.oauth.domain.OAuth2Profile;
import se.sowl.progdomain.oauth.domain.Provider;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;
import java.util.Map;

import static java.util.Collections.singleton;

@Service
@RequiredArgsConstructor
public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService oAuth2UserService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = getOAuth2User(oAuth2UserRequest);
        OAuth2Profile oAuthProfile = extractOAuth2Profile(oAuth2UserRequest, oAuth2User.getAttributes());
        User user = userRepository.findByEmailAndProvider(oAuthProfile.getEmail(), oAuthProfile.getProvider())
            .orElseGet(() -> saveUser(oAuthProfile));
        return createOAuth2User(oAuth2UserRequest, oAuth2User.getAttributes(), oAuthProfile, user);
    }

    private OAuth2User getOAuth2User(OAuth2UserRequest userRequest) {
        return oAuth2UserService.loadUser(userRequest);
    }

    private OAuth2Profile extractOAuth2Profile(OAuth2UserRequest oAuth2UserRequest, Map<String, Object> oAuthUserAttributes) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        Provider provider = Provider.valueOf(registrationId.toUpperCase());
        OAuth2Profile OAuthUserProfile = OAuth2Extractor.extract(provider, oAuthUserAttributes);
        OAuthUserProfile.setProvider(registrationId);
        return OAuthUserProfile;
    }

    private User saveUser(OAuth2Profile userProfile) {
        return userRepository.save(userProfile.toUser());
    }

    private OAuth2User createOAuth2User(
        OAuth2UserRequest userRequest, Map<String, Object> oAuth2UserAttributes, OAuth2Profile userProfile, User user
    ) {
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        OAuth2Attribute userAttributes = new OAuth2Attribute(oAuth2UserAttributes, userNameAttributeName, userProfile, user.getProvider());
        return new DefaultOAuth2User(singleton(new SimpleGrantedAuthority("USER")), userAttributes.getAttributes(), userNameAttributeName);
    }
}
