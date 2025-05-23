package dataaccess.implementation;

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
    public void createUser(UserData username) {
        store.put(username.password(), username);
    }

    @Override
    public Optional<UserData> findByPassword(String password) {
        return Optional.ofNullable(store.get(password));
    }

    @Override
    public List<UserData> findAll() {
        return new ArrayList<>(store.values());
    }
}
