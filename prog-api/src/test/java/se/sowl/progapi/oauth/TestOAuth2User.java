package se.sowl.progapi.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class TestOAuth2User implements OAuth2User {
    private Map<String, Object> attributes;
    private Collection<? extends GrantedAuthority> authorities;

    public TestOAuth2User(String name, String email) {
        this.attributes = Map.of(
            "sub", "123456",
            "name", name,
            "email", email
        );
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }
}
