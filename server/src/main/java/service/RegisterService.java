package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class RegisterService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public Result register(Request request) {
        try {
            if (userDAO.getUser(request.username()).isPresent()) {
                throw new AlreadyTakenException("The username '" + request.username() + "' is already taken");
            }

        UserData newUser = new UserData(request.username(),
                hashPassword(request.password()),
                request.email()
        );

        userDAO.createUser(newUser);

            String authToken = authDAO.createAuth(request.username()).authToken();

            return new Result(authToken, request.username());
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during registration", e);
        }
    }

    private String hashPassword(String plain) {
        return Integer.toHexString(plain.hashCode()); 
    }

    public record Result(String authToken, String username) { }
    public record Request(String username, String password, String email) { }
}