package service;

public record ClearResult(boolean success, String message) {
    public boolean isSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
}
