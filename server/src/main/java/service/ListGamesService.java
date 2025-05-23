package service;

import dataaccess.*;
import model.*;

import java.util.List;

public class ListGamesService {
    private final GameDAO gameDAO;

    public ListGamesService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames(ListGamesRequest request) {
        List<GameData> games = gameDAO.listGames();
        var entries = games.stream().map(game ->
                                        new GameEntry(game.gameID(),
                                        game.gameName(),
                                        game.whiteUsername(),
                                        game.blackUsername())).toArray(GameEntry[]::new);
        return new ListGamesResult(entries);
    }

}
