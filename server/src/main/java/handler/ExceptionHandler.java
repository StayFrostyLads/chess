package handler;

import service.*;

public class ExceptionHandler {
    /**
     * Given any Exception, produce the error-message text I want to send
     * over WebSocket to the client.
     */
    public static String mapToErrorMessage(Throwable e) {
        return switch (e) {
            case BadRequestException badRequestException -> "Bad request: " + e.getMessage();
            case AuthenticationException authenticationException -> "Authentication error: " + e.getMessage();
            case ForbiddenException forbiddenException -> "Forbidden: " + e.getMessage();
            case ServerException serverException -> "Server error: " + e.getMessage();
            default -> "Internal error: " + e.getMessage();
        };
    }
}
