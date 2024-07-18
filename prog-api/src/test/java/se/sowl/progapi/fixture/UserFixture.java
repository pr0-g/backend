package se.sowl.progapi.fixture;

import se.sowl.progdomain.user.domain.User;

public class UserFixture {
    public static User createUser(Long id, String name, String nickname, String email, String provider) {
        return User.builder()
                .id(id)
                .name(name)
                .nickname(nickname)
                .email(email)
                .provider(provider)
                .build();
    }
}
