package dataaccess.implementation;

import dataaccess.AuthDAO;
import model.AuthData;
import java.util.*;

public class InMemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> store = new HashMap<>();

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        store.put(authToken, auth);
        return auth;
    }

    @Override
    public Optional<AuthData> getAuth(String authToken) {
        return Optional.ofNullable(store.get(authToken));
    }

    @Override
    public List<AuthData> findAll() {
        return new ArrayList<>(store.values());
    }
}
