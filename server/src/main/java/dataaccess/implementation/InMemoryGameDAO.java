package dataaccess.implementation;

import dataaccess.GameDAO;
import model.GameData;
import java.util.*;

public class InMemoryGameDAO implements GameDAO {
    private final Map<String, GameData> store = new HashMap<>();

    @Override
    public void clear() {
        store.clear();
    }
}
