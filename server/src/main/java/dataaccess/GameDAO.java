package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.*;

public interface GameDAO {
    void clear();
    void addGame(GameData game) throws DataAccessException;
    int createGame(GameData game) throws DataAccessException;
    Optional<GameData> getGame(int gameID);
    void joinGame(int gameID, String username, ChessGame.TeamColor color) throws DataAccessException;
    List<GameData> listGames();
}
