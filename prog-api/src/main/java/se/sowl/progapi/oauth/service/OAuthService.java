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
import se.sowl.progdomain.user.domain.OAuthAttributes;
import se.sowl.progdomain.user.domain.UserProfile;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.singleton;

@Service
@RequiredArgsConstructor
public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = getOAuth2User(userRequest);
        UserProfile userProfile = extractUserProfile(userRequest, oAuth2User.getAttributes());
        System.out.println("userProfile = " + userProfile.getEmail() + userProfile.getProvider());
        User user = userRepository.findByEmailAndProvider(userProfile.getEmail(), userProfile.getProvider())
            .map(existingUser -> updateUser(existingUser, userProfile))
            .orElseGet(() -> saveUser(userProfile));
        return createOAuth2User(userRequest, oAuth2User.getAttributes(), userProfile, user);
    }

    // TODO: 인터페이스로 분리 -> 테스트를 위해 public 이 되면 안된다.
    public OAuth2User getOAuth2User(OAuth2UserRequest userRequest) {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        return delegate.loadUser(userRequest);
    }

    private UserProfile extractUserProfile(OAuth2UserRequest userRequest, Map<String, Object> attributes) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        UserProfile userProfile = OAuthAttributes.extract(registrationId, attributes);
        userProfile.setProvider(registrationId);
        return userProfile;
    }

    private User updateUser(User existingUser, UserProfile userProfile) {
        existingUser.update(userProfile.getName(), userProfile.getEmail());
        return userRepository.save(existingUser);
    }

    private User saveUser(UserProfile userProfile) {
        return userRepository.save(userProfile.toUser());
    }

    private OAuth2User createOAuth2User(
        OAuth2UserRequest userRequest, Map<String, Object> oAuth2UserAttributes, UserProfile userProfile, User user
    ) {
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> customAttribute = createCustomAttribute(oAuth2UserAttributes, userNameAttributeName, userProfile, user.getProvider());
        return new DefaultOAuth2User(singleton(new SimpleGrantedAuthority("USER")), customAttribute, userNameAttributeName);
    }

    private Map<String, Object> createCustomAttribute(Map<String, Object> attributes, String userNameAttributeName, UserProfile userProfile, String registrationId) {
        Map<String, Object> customAttribute = new LinkedHashMap<>();
        customAttribute.put(userNameAttributeName, attributes.get(userNameAttributeName));
        customAttribute.put("provider", registrationId);
        customAttribute.put("name", userProfile.getName());
        customAttribute.put("email", userProfile.getEmail());
        return customAttribute;
    }
}
