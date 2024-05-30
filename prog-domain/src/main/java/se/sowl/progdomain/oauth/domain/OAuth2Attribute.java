package se.sowl.progdomain.oauth.domain;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class OAuth2Attribute {
    private final Map<String, Object> attributes;

    public OAuth2Attribute(Map<String, Object> oAuth2UserAttributes, String userNameAttributeName, OAuth2Profile userProfile, String registrationId) {
        this.attributes = new LinkedHashMap<>(oAuth2UserAttributes);
        addAttribute(userNameAttributeName, oAuth2UserAttributes.get(userNameAttributeName));
        addAttribute("provider", registrationId);
        addAttribute("name", userProfile.getName());
        addAttribute("email", userProfile.getEmail());
    }

    private void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }
}
