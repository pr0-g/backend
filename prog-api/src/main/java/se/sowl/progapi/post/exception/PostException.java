package se.sowl.progapi.post.exception;

import org.springframework.http.HttpStatus;

public class PostException extends RuntimeException {
    private final HttpStatus status;

    public PostException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static class PostNotExistException extends PostException {
        public PostNotExistException() {
            super("존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND);
        }
    }

    public static class PostNotAuthorizedException extends PostException {
        public PostNotAuthorizedException() {
            super("게시글 수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
    }

    public static class PostContentNotExistException extends PostException {
        public PostContentNotExistException() {
            super("게시글 내용이 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }
    }
}