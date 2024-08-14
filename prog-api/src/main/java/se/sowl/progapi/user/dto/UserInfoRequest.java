package se.sowl.progapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.sowl.progapi.interest.dto.UserInterestRequest;

import java.util.List;


@Getter
@AllArgsConstructor
public class UserInfoRequest {
    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String provider;
    private List<UserInterestRequest> interests;
}