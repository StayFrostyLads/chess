package dataaccess.implementation;

import dataaccess.GameDAO;
import model.GameData;
import java.util.*;

public class InMemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> store = new HashMap<>();
    private int nextID = 1;

    @Override
    public void clear() {
        store.clear();
        nextID = 1;
    }

    @Override
    public void addGame(GameData game) {
        store.put(game.gameID(), game);
    }

    @Override
    public int createGame(GameData game) {
        int id = nextID++;
        GameData withID = new GameData(id, game.whiteUsername(),
                game.blackUsername(), game.gameName(), game.game()
        );
        store.put(id, withID);
        return id;
    }

    @Override
    public Optional<GameData> getGame(int gameID) {
        return Optional.ofNullable(store.get(gameID));
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(store.values());
    }
}
