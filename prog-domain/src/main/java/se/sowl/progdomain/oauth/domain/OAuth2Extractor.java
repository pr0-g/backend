package se.sowl.progdomain.oauth.domain;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public enum OAuth2Extractor {
    GOOGLE(OAuth2Provider.GOOGLE, OAuth2Extractor::extractGoogleProfile),
    NAVER(OAuth2Provider.NAVER, OAuth2Extractor::extractNaverProfile),
    KAKAO(OAuth2Provider.KAKAO, OAuth2Extractor::extractKakaoProfile);

    private final OAuth2Provider provider;
    private final Function<Map<String, Object>, OAuth2Profile> of;

    public static OAuth2Profile extract(OAuth2Provider provider, Map<String, Object> oAuth2UserAttributes) {
        return Arrays.stream(values())
            .filter(value -> provider.getRegistrationId().equals(value.provider.getRegistrationId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("잘못된 registrationId 입니다: " + provider.getRegistrationId()))
            .of.apply(oAuth2UserAttributes);
    }

    private static OAuth2Profile extractGoogleProfile(Map<String, Object> attributes) {
        return extractProfile(attributes);
    }

    private static OAuth2Profile extractNaverProfile(Map<String, Object> attributes) {
        Map<String, Object> response = getMapAttribute(attributes, "response");
        return extractProfile(response);
    }

    private static OAuth2Profile extractKakaoProfile(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = getMapAttribute(attributes, "kakao_account");
        Map<String, Object> kakaoProfile = getMapAttribute(kakaoAccount, "profile");
        return extractProfile(kakaoProfile, "nickname", kakaoAccount);
    }

    private static OAuth2Profile extractProfile(Map<String, Object> attributes, String nameKey, Map<String, Object> emailSource) {
        OAuth2Profile userProfile = new OAuth2Profile();
        userProfile.setName(getStringAttribute(attributes, nameKey));
        userProfile.setEmail(getStringAttribute(emailSource, "email"));
        return userProfile;
    }

    private static OAuth2Profile extractProfile(Map<String, Object> attributes) {
        return extractProfile(attributes, "name", attributes);
    }

    private static String getStringAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalArgumentException("키에 대한 값이 문자열 이어야 합니다: " + key);
    }

    private static Map<String, Object> getMapAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        throw new IllegalArgumentException("키에 대한 값이 Map 타입 이여야 합니다: " + key);
    }
}
