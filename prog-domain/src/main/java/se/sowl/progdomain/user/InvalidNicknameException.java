package se.sowl.progdomain.user;

public class InvalidNicknameException extends RuntimeException {
    public InvalidNicknameException(String message) {
        super(message);
    }
}
