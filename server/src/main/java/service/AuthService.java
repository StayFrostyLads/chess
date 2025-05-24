package service;

import dataaccess.*;
import model.*;
import java.util.Optional;

public class AuthService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    public AuthService(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    public AuthData validateAuthToken(String authToken) {
        try {
            if (authToken == null || authToken.isBlank()) {
                throw new AuthenticationException("Missing auth token");
            }
            return authDAO.getAuth(authToken).orElseThrow(
                    () -> new AuthenticationException("Invalid auth token"));
        } catch (DataAccessException e) {
            throw new ServerException("Failed to access auth data", e);
        }
    }

    public ClearResult clearDatabase() {
        try {
            authDAO.clear();
            gameDAO.clear();
            userDAO.clear();
            return new ClearResult(true, "Database successfully cleared!");
        } catch (Exception e) {
            throw new ClearFailedException("Could not clear the database", e);
        }
    }

    public record ClearResult(boolean success, String message) { }
}
