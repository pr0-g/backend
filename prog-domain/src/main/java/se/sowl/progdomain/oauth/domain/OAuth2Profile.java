package se.sowl.progdomain.oauth.domain;

import lombok.Getter;
import lombok.Setter;
import se.sowl.progdomain.user.domain.User;

@Getter
@Setter
public class OAuth2Profile {
    private String name;
    private String email;
    private String provider;
    private String nickname;

    public User toUser() {
        return User.builder()
            .name(name)
            .email(email)
            .provider(provider)
            .build();
    }
}
