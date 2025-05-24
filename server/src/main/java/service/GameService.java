package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;

import java.util.List;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthService authService;

    public GameService(GameDAO gameDAO, AuthService authService) {
        this.gameDAO = gameDAO;
        this.authService = authService;
    }

    public CreateGameResult createGame(String authToken, String gameName) {
        try {
            AuthData auth = authService.validateAuthToken(authToken);

            GameData newGame = new GameData(0, auth.username(),
                    null, gameName, new ChessGame()
            );
            int gameID = gameDAO.createGame(newGame);
            return new CreateGameResult(gameID);
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during game creation", e);
        }
    }

    public JoinGameResult joinGame(String authToken, int gameID, String playerColor) {
        try {
            AuthData auth = authService.validateAuthToken(authToken);
            GameData game = gameDAO.getGame(gameID).orElseThrow(
                    () -> new BadRequestException("Game not found: " + gameID)
            );
            ChessGame.TeamColor color;
            try {
                color = ChessGame.TeamColor.valueOf(playerColor.toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException("Invalid team color: " + playerColor);
            }

            if (color == ChessGame.TeamColor.WHITE && game.whiteUsername() != null) {
                throw new ForbiddenException("Someone is already playing as white!");
            }
            if (color == ChessGame.TeamColor.BLACK && game.blackUsername() != null) {
                throw new ForbiddenException("Someone is already playing as black!");
            }

            gameDAO.joinGame(gameID, auth.username(), color);
            return new JoinGameResult();
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error while joining game", e);
        }
    }

    public ListGamesResult listGames(String authToken) {
        try {
            authService.validateAuthToken(authToken);
            List<GameData> games = gameDAO.listGames();
            GameEntry[] entries = games.stream().map(game ->
                    new GameEntry(game.gameID(),
                            game.gameName(),
                            game.whiteUsername(),
                            game.blackUsername())).toArray(GameEntry[]::new);
            return new ListGamesResult(entries);
        } catch (RuntimeException e) {
            throw new ServerException("Database connection error while listing games", e);
        }
    }

    public record CreateGameResult(int gameID) { }
    public record JoinGameResult() { }
    public record ListGamesResult(GameEntry[] games) { }
    public record GameEntry(int gameID, String gameName, String whiteUsername, String blackUsername) { }

    public record CreateGameRequest(String gameName) { }
    public record JoinGameRequest(String playerColor, int gameID) { }
}
