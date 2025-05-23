package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import java.util.Optional;

public class AuthService {
    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public AuthData validate(String authToken) {
        try {
            Optional<AuthData> authOpt = authDAO.getAuth(authToken);
            if (authOpt.isEmpty()) {
                throw new AuthenticationException("Invalid authToken");
            }
            return authOpt.get();
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during registration", e);
        }
    }
}
