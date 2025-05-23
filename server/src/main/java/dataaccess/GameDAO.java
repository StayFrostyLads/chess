package dataaccess;

import model.GameData;
import java.util.*;

public interface GameDAO {
    void clear();
    void createGame(GameData game) throws DataAccessException;
    Optional<GameData> getGame(int gameID);
    List<GameData> listGames();
}
