package dataaccess.implementation;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

import java.util.*;

public class InMemoryUserDAO implements UserDAO {
    private final Map<String, UserData> store = new HashMap<>();

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException{
        if (store.containsKey(user.username())) {
            throw new DataAccessException("Username already exists");
        }
        store.put(user.username(), user);
    }

    @Override
    public Optional<UserData> getUser(String username) {
        return Optional.ofNullable(store.get(username));
    }

    @Override
    public List<UserData> findAll() {
        return new ArrayList<>(store.values());
    }
}
