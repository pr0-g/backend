package se.sowl.progapi.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import se.sowl.progdomain.user.InvalidNicknameException;


@Slf4j
@ControllerAdvice
public class RestControllerAdvice {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<Void> handleBadRequest(Exception e) {
        log.error("Bad Request Exception", e);
        return CommonResponse.fail(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException", e);
        return CommonResponse.fail("서버에 문제가 생겼어요. 잠시 후 다시 시도해주세요.");
    }

    @ExceptionHandler(InvalidNicknameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<Void> handleInvalidNicknameException(InvalidNicknameException e) {
        log.error("Invalid Nickname Exception", e);
        return CommonResponse.fail(e.getMessage());
    }
}