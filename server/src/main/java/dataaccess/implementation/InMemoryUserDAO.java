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
}

