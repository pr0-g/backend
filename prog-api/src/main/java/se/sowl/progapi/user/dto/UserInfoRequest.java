package se.sowl.progapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.sowl.progdomain.interest.domain.UserInterest;

import java.util.Set;

@Getter
@AllArgsConstructor
public class UserInfoRequest {
    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String provider;
    private Set<UserInterest> userInterests;
}