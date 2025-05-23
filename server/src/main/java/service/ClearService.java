package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class ClearService {
    private final AuthDAO authDao;
    private final GameDAO gameDao;
    private final UserDAO userDao;

    public ClearService(AuthDAO authDao, GameDAO gameDao, UserDAO userDAO) {
        this.authDao = authDao;
        this.gameDao = gameDao;
        this.userDao = userDAO;
    }

    public ClearResult clear(ClearRequest req) {
        try {
            authDao.clear();
            gameDao.clear();
            userDao.clear();
        } catch (Exception e) {
            throw new ClearFailedException("Could not clear the database", e);
        }
        return new ClearResult(true, "Database successfully cleared!");
    }

}
