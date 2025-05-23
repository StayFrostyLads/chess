package service;

import dataaccess.*;
import model.UserData;
import model.AuthData;
import java.util.Optional;

public class LoginService {
    private final UserDAO userDao;
    private final AuthDAO authDao;

    public LoginService(UserDAO userDao, AuthDAO authDao) {
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public Result login(Request request) {
        try {
            Optional<UserData> possibleUser = userDao.getUser(request.username());
            if (possibleUser.isEmpty()) {
                throw new AuthenticationException("Invalid username");
            }
            UserData user = possibleUser.get();

            String inputHash = hashPassword(request.password());
            if (!user.password().equals(inputHash)) {
                throw new AuthenticationException("Invalid password");
            }

            AuthData auth = authDao.createAuth(user.username());

            return new Result(auth.authToken(), user.username());
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during registration", e);
        }
    }

    private String hashPassword(String plain) {
        return Integer.toHexString(plain.hashCode());
    }

    public record Result(String authToken, String username) { }
    public record Request(String username, String password) { }
}
