package se.sowl.progapi.interest.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditInterestRequest {
    @NotEmpty(message = "관심사 ID 목록은 비어있을 수 없습니다.")
    private List<Long> interestIdList;

    // 기본 생성자
    public EditInterestRequest() {}

    // 모든 필드를 포함한 생성자
    public EditInterestRequest(List<Long> interestIdList) {
        this.interestIdList = interestIdList;
    }
}