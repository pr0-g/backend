package se.sowl.progdomain.user.domain;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public enum OAuthAttributes {
    GOOGLE("google", OAuthAttributes::extractGoogleProfile),
    NAVER("naver", OAuthAttributes::extractNaverProfile),
    KAKAO("kakao", OAuthAttributes::extractKakaoProfile);

    private final String registrationId;
    private final Function<Map<String, Object>, UserProfile> of;

    public static UserProfile extract(String registrationId, Map<String, Object> attributes) {
        return Arrays.stream(values())
            .filter(provider -> registrationId.equals(provider.registrationId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid registrationId: " + registrationId))
            .of.apply(attributes);
    }

    private static UserProfile extractGoogleProfile(Map<String, Object> attributes) {
        UserProfile userProfile = new UserProfile();
        userProfile.setName((String) attributes.get("name"));
        userProfile.setEmail((String) attributes.get("email"));
        return userProfile;
    }

    private static UserProfile extractNaverProfile(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        UserProfile userProfile = new UserProfile();
        userProfile.setName((String) response.get("name"));
        userProfile.setEmail((String) response.get("email"));
        return userProfile;
    }

    private static UserProfile extractKakaoProfile(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
        UserProfile userProfile = new UserProfile();
        userProfile.setName((String) kakaoProfile.get("nickname"));
        userProfile.setEmail((String) kakaoAccount.get("email"));
        return userProfile;
    }
}
