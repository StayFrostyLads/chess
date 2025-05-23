package handler;

import java.util.UUID;

public class LoginHandler {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    class Response {
        String message;
    }

    class LoginResponse extends Response {
        String authToken;
        String username;

    }

}
