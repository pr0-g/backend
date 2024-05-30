package se.sowl.progdomain.oauth.domain;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public enum OAuth2Extractor {
    GOOGLE(Provider.GOOGLE, OAuth2Extractor::extractGoogleProfile),
    NAVER(Provider.NAVER, OAuth2Extractor::extractNaverProfile),
    KAKAO(Provider.KAKAO, OAuth2Extractor::extractKakaoProfile);

    private final Provider provider;
    private final Function<Map<String, Object>, OAuth2Profile> of;

    public static OAuth2Profile extract(Provider provider, Map<String, Object> oAuthUserAttributes) {
        return Arrays.stream(values())
            .filter(value -> provider.getRegistrationId().equals(value.provider.getRegistrationId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid registrationId: " + provider.getRegistrationId()))
            .of.apply(oAuthUserAttributes);
    }

    private static OAuth2Profile extractGoogleProfile(Map<String, Object> attributes) {
        OAuth2Profile userProfile = new OAuth2Profile();
        userProfile.setName((String) attributes.get("name"));
        userProfile.setEmail((String) attributes.get("email"));
        return userProfile;
    }

    private static OAuth2Profile extractNaverProfile(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        OAuth2Profile userProfile = new OAuth2Profile();
        userProfile.setName((String) response.get("name"));
        userProfile.setEmail((String) response.get("email"));
        return userProfile;
    }

    private static OAuth2Profile extractKakaoProfile(Map<String, Object> attributes) {
        System.out.println(attributes.toString());
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
        OAuth2Profile userProfile = new OAuth2Profile();
        userProfile.setName((String) kakaoProfile.get("nickname"));
        userProfile.setEmail((String) kakaoAccount.get("email"));
        return userProfile;
    }
}
