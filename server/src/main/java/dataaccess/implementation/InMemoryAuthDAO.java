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
}
