package se.sowl.progdomain.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.sowl.progdomain.user.InvalidNicknameException;
import se.sowl.progdomain.interest.domain.UserInterest;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하여야 합니다.")
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<UserInterest> userInterest;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(Long id, String name, String nickname, String email, String provider) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.userInterest = Set.of();
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }


    
    public void updateNickname(String nickname) {
        log.debug("Updating nickname to: {}", nickname);
        if (nickname.length() < 2 || nickname.length() > 15) {
            log.error("Invalid nickname length: {}", nickname.length());
            throw new InvalidNicknameException("닉네임은 2자 이상 15자 이하여야 합니다.");
        }


        this.nickname = nickname;
    }


    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}