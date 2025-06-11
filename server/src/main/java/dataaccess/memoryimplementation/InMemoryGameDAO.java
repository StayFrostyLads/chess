package dataaccess.memoryimplementation;

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
    public GameData createGame(String gameName) {
        int id = nextID++;
        ChessGame newGame = new ChessGame();
        GameData gameData = new GameData(id, null,
                null, gameName, newGame
        );
        store.put(id, gameData);
        return gameData;
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

    @Override
    public void saveGame(int gameID, ChessGame updatedGame) throws DataAccessException {
        GameData oldData = store.get(gameID);
        if (oldData == null) {
            throw new DataAccessException("Cannot save non-existent game! " + gameID);
        }
        store.put(gameID, new GameData(gameID, oldData.whiteUsername(),
                oldData.blackUsername(), oldData.gameName(), updatedGame));
    }

    @Override
    public void leaveGame(int gameID, String username) throws DataAccessException {
        GameData oldData = store.get(gameID);
        if (oldData == null) {
            throw new DataAccessException("No such game exists with ID: " + gameID);
        }
        String white = oldData.whiteUsername();
        String black = oldData.blackUsername();
        if (username.equals(white)) {
            white = null;
        } else if (username.equals(black)) {
            black = null;
        } else {
            return;
        }
        store.put(gameID, new GameData(gameID, white, black, oldData.gameName(), oldData.game()));
    }
}