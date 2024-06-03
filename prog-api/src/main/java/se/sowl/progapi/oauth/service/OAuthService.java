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
import se.sowl.progdomain.oauth.domain.*;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService oAuth2UserService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(oAuth2UserRequest);
        OAuth2Profile oAuth2Profile = extractOAuth2Profile(registrationId, oAuth2User.getAttributes());
        User user = getOrCreateUser(oAuth2Profile);
        return createOAuth2User(oAuth2UserRequest, oAuth2User.getAttributes(), oAuth2Profile, user);
    }

    private User getOrCreateUser(OAuth2Profile oAuth2Profile) {
        return userRepository.findByEmailAndProvider(oAuth2Profile.getEmail(), oAuth2Profile.getProvider())
            .orElseGet(() -> userRepository.save(oAuth2Profile.toUser()));
    }

    private OAuth2Profile extractOAuth2Profile(String registrationId, Map<String, Object> oAuth2UserAttributes) {
        OAuth2Provider provider = OAuth2Provider.valueOf(registrationId.toUpperCase());
        OAuth2Profile OAuthUserProfile = OAuth2Extractor.extract(provider, oAuth2UserAttributes);
        OAuthUserProfile.setProvider(registrationId);
        return OAuthUserProfile;
    }

    private OAuth2User createOAuth2User(
        OAuth2UserRequest userRequest, Map<String, Object> oAuth2Attributes, OAuth2Profile userProfile, User user
    ) {
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        OAuth2UserAttribute oAuth2UserAttribute = new OAuth2UserAttribute(oAuth2Attributes, userNameAttributeName, userProfile, user.getProvider());
        Collection<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(Role.USER.getValue()));
        return new DefaultOAuth2User(authorities, oAuth2UserAttribute.getAttributes(), userNameAttributeName);
    }
}
