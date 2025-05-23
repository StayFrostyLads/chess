package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;

import java.util.List;

public class CreateGameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public CreateGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public Result createGame(Request request) {
        try {
            if (request == null || request.authToken() == null || request.authToken().isBlank()) {
                throw new AuthenticationException("Missing auth token");
            }

            AuthData auth = authDAO.getAuth(request.authToken()).orElseThrow(
                    ()-> new AuthenticationException("Invalid auth token"));

            GameData newGame = new GameData(0, auth.username(),
                    null, request.gameName(), new ChessGame()
            );
            int id = gameDAO.createGame(newGame);
            return new Result(id);
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during registration", e);
        }

    }

    public record Request(String authToken, String gameName) { }
    public record Result(int gameID) { }

}

