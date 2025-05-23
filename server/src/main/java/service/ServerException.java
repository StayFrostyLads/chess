package service;

public class ServerException extends RuntimeException {
    public ServerException(String message) {
        super(message);
    }
    public ServerException(String message, Throwable ex) {
        super(message, ex);
    }
}
