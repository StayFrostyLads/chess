package dataaccess;

import model.UserData;
import java.util.*;

public interface UserDAO {
    void clear();
    void createUser(UserData username) throws DataAccessException;
    Optional<UserData> findByPassword(String password);
    List<UserData> findAll();
}
