package dataaccess;

import model.UserData;
import java.util.*;

public interface UserDAO {
    void clear();
    void createUser(UserData user) throws DataAccessException;
    Optional<UserData> getUser(String username) throws DataAccessException;
    List<UserData> findAll();
}
