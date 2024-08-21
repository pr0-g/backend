package se.sowl.progapi.post.exception;

public class PostException extends RuntimeException {

    public PostException(String message) {
        super(message);
    }

    public static class PostNotExistException extends PostException {
        public PostNotExistException() {
            super("존재하지 않는 게시글입니다.");
        }
    }

    public static class PostNotAuthorizedException extends PostException {
        public PostNotAuthorizedException() {
            super("게시글 수정 권한이 없습니다.");
        }
    }

    public static class PostContentNotExistException extends PostException {
        public PostContentNotExistException() {
            super("게시글 내용이 존재하지 않습니다.");
        }
    }


}