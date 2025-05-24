package dataaccess.implementation;

import chess.ChessGame;
import dataaccess.DataAccessException;
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
    public void joinGame(int gameID, String username, ChessGame.TeamColor color) throws DataAccessException{
        GameData oldData = store.get(gameID);
        if (oldData == null) {
            throw new DataAccessException("No such game: " + gameID);
        }
        String white = oldData.whiteUsername();
        String black = oldData.blackUsername();
        if (color == ChessGame.TeamColor.WHITE) {
            white = username;
        } else {
            black = username;
        }
        GameData updatedData = new GameData(oldData.gameID(), white, black,
                                            oldData.gameName(), oldData.game()
        );
        store.put(gameID, updatedData);
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(store.values());
    }
}
