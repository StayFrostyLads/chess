package server;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;
    
    private String authToken;

    public ServerFacade(String url) {
        serverUrl = url;
    }
    
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public AuthResult register(RegisterRequest request) {
        return makeRequest(
                "POST",
                "/user",
                request,
                AuthResult.class,
                /* includeAuthHeader= */ false
        );
    }
    
    public AuthResult login(LoginRequest request) {
        AuthResult result = makeRequest(
                "POST",
                "/session",
                request,
                AuthResult.class,
                /* includeAuthHeader= */ false
        );
        setAuthToken(result.authToken());
        return result;
    }

    public LogoutResult logout() {
        return makeRequest(
                "DELETE",
                "/session",
                null,
                LogoutResult.class,
                /* includeAuthHeader= */ true
        );
    }

    public ListGamesResult listGames() {
        return makeRequest(
                "GET",
                "/game",
                null,
                ListGamesResult.class,
                /* includeAuthHeader= */ true
        );
    }

    public CreateGameResult createGame(CreateGameRequest request) {
        return makeRequest(
                "POST",
                "/game",
                request,
                CreateGameResult.class,
                /* includeAuthHeader= */ true
        );
    }

    public JoinGameResult joinGame(JoinGameRequest request) {
        return makeRequest(
                "PUT",
                "/game",
                request,
                JoinGameResult.class,
                /* includeAuthHeader= */ true
        );
    }

    public ObserveGameResult observeGame(int gameID) {
        String path = "/game/" + gameID;
        return makeRequest(
                "GET",
                path,
                null,
                ObserveGameResult.class,
                /* includeAuthHeader= */ true
        );
    }

    public ClearResult clearDatabases() {
        return makeRequest(
                "DELETE",
                "/db",
                null,
                ClearResult.class,
                /* includeAuthHeader= */ false
        );
    }

    /**
     * @param method            HTTP Methods: "GET", "POST", "PUT", or "DELETE"
     * @param path              The desired url path where the http method is called
     * @param requestObj        The serialized POJO that sends as the body
     * @param responseClass     The deserialized class
     * @param includeAuthHeader Determines whether the program looks at the "Authorization" header
     *
     * @return The deserialized response
     */
    private <T> T makeRequest(String method, String path, Object requestObj,
                              Class<T> responseClass, boolean includeAuthHeader) {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (includeAuthHeader && authToken != null && !authToken.isBlank()) {
                http.setRequestProperty("Authorization", authToken);
            }

            writeBody(requestObj, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (IOException e) {
            throw new ServerException("Network/IO error: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL given: " + e.getMessage());
        }
    }

    private static void writeBody(Object requestObj, HttpURLConnection http) throws IOException {
        if (requestObj != null) {
            http.setRequestProperty("Content-Type", "application/json");
            String json = new Gson().toJson(requestObj);
            try (OutputStream os = http.getOutputStream()) {
                os.write(json.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException {
        int status = http.getResponseCode();
        if (!isSuccessful(status)) {
            String raw;
            try (InputStream errStream = http.getErrorStream()) {
                if (errStream == null) {
                    raw = "";
                } else {
                    raw = new String(errStream.readAllBytes());
                }
            }
            String serverMessage = null;
            if (!raw.isBlank()) {
                try {
                    ResponseException rex = ResponseException.fromJson(
                            new ByteArrayInputStream(raw.getBytes())
                    );
                    serverMessage = rex.getMessage();
                } catch (Exception parseEx) {
                    serverMessage = raw.trim();
                }
            }
            if (serverMessage == null || serverMessage.isEmpty()) {
                serverMessage = "HTTP " + status;
            }
            switch (status) {
                case 400:
                    throw new BadRequestException(serverMessage);
                case 401:
                    throw new AuthenticationException(serverMessage);
                case 403:
                    if (serverMessage.toLowerCase().contains("already taken")) {
                        throw new AlreadyTakenException(serverMessage);
                    } else {
                        throw new ForbiddenException(serverMessage);
                    }
                case 500:
                    throw new ServerException(serverMessage);
                default:
                    throw new RuntimeException("Unexpected HTTP status " + status + ": " + serverMessage);
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        if (responseClass == null) {
            return null;
        }
        try (InputStream in = http.getInputStream()) {
            return new Gson().fromJson(new InputStreamReader(in), responseClass);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) { super(message); }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) { super(message); }
    }

    public static class AlreadyTakenException extends RuntimeException {
        public AlreadyTakenException(String message) { super(message); }
    }

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) { super(message); }
    }

    public static class ServerException extends RuntimeException {
        public ServerException(String message) { super(message); }
    }

    public record RegisterRequest(String username, String password, String email) { }
    public record LoginRequest(String username, String password) { }
    public record AuthResult(boolean success, String authToken, String username) { }
    public record LogoutResult(boolean success, String message) { }
    public record CreateGameRequest(String gameName) { }
    public record CreateGameResult(boolean success, Integer gameID, GameEntry game) { }
    public record JoinGameRequest(int gameID, String playerColor) { }
    public record JoinGameResult(boolean success, GameEntry game) { }
    public record GameEntry(int gameID, String gameName, String whiteUsername, String blackUsername) { }
    public record ListGamesResult(boolean success, GameEntry[] games) { }
    public record ObserveGameResult(boolean success, ChessGame game, String message) { }
    public record ClearResult(boolean success, String message) { }

}
