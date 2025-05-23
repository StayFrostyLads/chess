package dataaccess;

import model.GameData;
import java.util.*;

public interface GameDAO {
    void clear();
    void addGame(GameData game) throws DataAccessException;
    int createGame(GameData game) throws DataAccessException;
    Optional<GameData> getGame(int gameID);
    List<GameData> listGames();
}
