package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;

import java.util.List;

public class JoinGameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public JoinGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public Result joinGame(Request request) {
        if (request == null || request.authToken() == null || request.authToken().isBlank()) {
            throw new AuthenticationException("Missing auth token");
        }

        try {

            AuthData auth = authDAO.getAuth(request.authToken()).orElseThrow(
                    ()-> new AuthenticationException("Invalid auth token"));
            GameData game = gameDAO.getGame(request.gameID()).orElseThrow(
                    () -> new BadRequestException("Game not found: " + request.gameID())
            );
            ChessGame.TeamColor color;
            try {
                color = ChessGame.TeamColor.valueOf(request.playerColor().toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException("Invalid team color: " + request.playerColor());
            }

            if (color == ChessGame.TeamColor.WHITE && game.whiteUsername() != null) {
                throw new ForbiddenException("Someone is already playing as white!");
            }
            if (color == ChessGame.TeamColor.BLACK && game.blackUsername() != null) {
                throw new ForbiddenException("Someone is already playing as black!");
            }

            gameDAO.joinGame(request.gameID(), auth.username(), color);

            return new Result();
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during registration", e);
        }

    }

    public record Request(String authToken, String playerColor, int gameID) { }
    public record Result() { }

}

