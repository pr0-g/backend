package se.sowl.progapi.user.exception;

import org.springframework.http.HttpStatus;
import se.sowl.progapi.post.exception.PostException;

public class UserException extends RuntimeException {
    private final HttpStatus status;

    public UserException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static class UserNotExistException extends UserException {
        public UserNotExistException() {
            super("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND);
        }
    }
}
