package dataaccess;

import model.AuthData;
import java.util.*;

public interface AuthDAO {
    void clear();
    void insert(AuthData auth) throws DataAccessException;
    Optional<AuthData> findByToken(String authToken);
    List<AuthData> findAll();
}
