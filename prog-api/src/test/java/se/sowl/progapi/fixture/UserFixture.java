package se.sowl.progapi.fixture;

import se.sowl.progapi.oauth.TestOAuth2User;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;
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

    public static CustomOAuth2User createCustomOAuth2User(User user) {
        TestOAuth2User testOAuth2User = new TestOAuth2User(user.getName(), user.getEmail());
        return new CustomOAuth2User(user, testOAuth2User);
    }
}
