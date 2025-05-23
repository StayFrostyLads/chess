package service;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
    public AuthenticationException(String message, Throwable ex) { super(message, ex); }
}
