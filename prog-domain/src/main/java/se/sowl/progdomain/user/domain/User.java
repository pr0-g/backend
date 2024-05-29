package se.sowl.progdomain.user.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider;

    @Builder
    public User(Long id, String name, String nickname, String email, String provider) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
    }

    public User update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }
}
