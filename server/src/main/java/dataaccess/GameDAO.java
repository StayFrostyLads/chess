package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.*;

public interface GameDAO {
    void clear() throws DataAccessException;
    void addGame(GameData game) throws DataAccessException;
    GameData createGame(String gameName) throws DataAccessException;
    Optional<GameData> getGame(int gameID) throws DataAccessException;
    void joinGame(int gameID, String username, ChessGame.TeamColor color) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void saveGame(int gameID, ChessGame updatedGame) throws DataAccessException;
}
