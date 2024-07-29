package se.sowl.progapi.interest.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EditUserInterestRequest {
    @NotEmpty(message = "관심사 ID 목록은 비어있을 수 없습니다.")
    private List<Long> interestIdList;

    public EditUserInterestRequest(List<Long> interestIdList) {
        this.interestIdList = interestIdList;
    }
}