package service;

import dataaccess.*;

public class LogoutService {
    private final AuthDAO authDao;

    public LogoutService(AuthDAO authDao) {
        this.authDao = authDao;
    }

    public Result logout(Request request) {
        try {
            boolean removed = authDao.removeToken(request.authToken());
            if (!removed) {
                throw new AuthenticationException("Invalid auth token: " + request.authToken());
            }
            return new Result(true, "User successfully logged out");
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during registration", e);
        }

    }

    public static record Request(String authToken) { }
    public static record Result(boolean success, String message) { }
}
