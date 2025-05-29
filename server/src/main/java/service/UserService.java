package service;

import dataaccess.*;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final AuthService authService;

    public UserService(UserDAO userDAO, AuthDAO authDAO, AuthService authService) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.authService = authService;
    }

    public AuthResult register(String username, String password, String email) {
        try {
            if (username == null || username.isBlank()) {
                throw new BadRequestException("Please choose a username");
            }
            if (password == null || password.isBlank()) {
                throw new BadRequestException("Please choose a password");
            }
            if (email == null || email.isBlank()) {
                throw new BadRequestException("Please insert an email");
            }

            if (userDAO.getUser(username).isPresent()) {
                throw new AlreadyTakenException("The username '" + username + "' is already taken");
            }

            String hashedPassword = hashPassword(password);
            UserData newUser = new UserData(username, hashedPassword, email);
            userDAO.createUser(newUser);

            String authToken = authDAO.createAuth(username).authToken();

            return new AuthResult(authToken, username);
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during registration", e);
        }
    }

    public AuthResult login(String username, String password) {
        try {
            if (username == null || username.isBlank()) {
                throw new BadRequestException("Please input a valid, existing username");
            }
            if (password == null || password.isBlank()) {
                throw new BadRequestException("Please insert a password");
            }
            Optional<UserData> possibleUser = userDAO.getUser(username);
            if (possibleUser.isEmpty()) {
                throw new AuthenticationException("Invalid username");
            }

            UserData user = possibleUser.get();
            if (!verifyPassword(password, user.password())) {
                throw new AuthenticationException("Invalid password");
            }


            AuthData auth = authDAO.createAuth(user.username());
            return new AuthResult(auth.authToken(), user.username());
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during login", e);
        }
    }

    public LogoutResult logout(String authToken) {
        try {
            authService.validateAuthToken(authToken);
            authDAO.removeToken(authToken);
            return new LogoutResult(true, "User successfully logged out");
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during logout", e);
        }
    }

    private String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    private boolean verifyPassword(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }

    public record AuthResult(String authToken, String username) { }
    public record LogoutResult(boolean success, String message) { }

    public record RegisterRequest(String username, String password, String email) { }
    public record LoginRequest(String username, String password) { }
}
