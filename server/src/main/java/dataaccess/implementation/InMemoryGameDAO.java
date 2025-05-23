package dataaccess.implementation;

import dataaccess.GameDAO;
import model.GameData;
import java.util.*;

public class InMemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> store = new HashMap<>();

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public void createGame(GameData game) {
        store.put(game.gameID(), game);
    }

    @Override
    public Optional<GameData> findByGameId(int gameID) {
        return Optional.ofNullable(store.get(gameID));
    }

    @Override
    public List<GameData> findAll() {
        return new ArrayList<>(store.values());
    }
}
