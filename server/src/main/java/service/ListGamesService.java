package service;

import dataaccess.*;
import model.*;

import java.util.List;

public class ListGamesService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ListGamesService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGamesResult listGames(ListGamesRequest request) {
        try {
            if (request == null || request.authToken() == null || request.authToken().isBlank()) {
                throw new AuthenticationException("Missing auth token");
            }

            AuthData auth = authDAO.getAuth(request.authToken()).orElseThrow(
                    ()-> new AuthenticationException("Invalid auth token"));

            List<GameData> games = gameDAO.listGames();
            var entries = games.stream().map(game ->
                    new GameEntry(game.gameID(),
                            game.gameName(),
                            game.whiteUsername(),
                            game.blackUsername())).toArray(GameEntry[]::new);
            return new ListGamesResult(entries);
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during registration", e);
        }

    }

}
