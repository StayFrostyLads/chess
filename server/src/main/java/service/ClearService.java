package service;

import dataaccess.*;

public class ClearService {
    private final AuthDAO authDao;
    private final GameDAO gameDao;
    private final UserDAO userDao;

    public ClearService(AuthDAO authDao, GameDAO gameDao, UserDAO userDAO) {
        this.authDao = authDao;
        this.gameDao = gameDao;
        this.userDao = userDAO;
    }

    public Result clear() {
        try {
            authDao.clear();
            gameDao.clear();
            userDao.clear();
        } catch (Exception e) {
            throw new ClearFailedException("Could not clear the database", e);
        }
        return new Result(true, "Database successfully cleared!");
    }

    public record Result(boolean success, String message) { }
    public record Request() { }

}
