package se.sowl.progapi.interest.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditUserInterestRequest {
    @NotEmpty(message = "관심사 ID 목록은 비어있을 수 없습니다.")
    private List<Long> interestIdList;
}
