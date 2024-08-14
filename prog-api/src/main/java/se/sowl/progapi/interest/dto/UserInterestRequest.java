package se.sowl.progapi.interest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInterestRequest {
    private Long id;
    private String name;
}