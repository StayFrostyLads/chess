package dataaccess;

import model.AuthData;

import java.util.*;

public interface AuthDAO {
    void clear();
    AuthData createAuth(String username) throws DataAccessException;
    Optional<AuthData> getAuth(String authToken) throws DataAccessException;
    List<AuthData> findAll();
    boolean removeToken(String authToken) throws DataAccessException;
}
