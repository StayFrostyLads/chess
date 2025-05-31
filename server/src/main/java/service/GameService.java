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

    public CreateGameResult createGame(String gameName, String authToken) {
        try {
            AuthData auth = authService.validateAuthToken(authToken);
            if (gameName == null || gameName.isBlank()) {
                throw new BadRequestException("Game name can't be null or empty");
            }
            GameData newGame = gameDAO.createGame(gameName);
            gameDAO.joinGame(newGame.gameID(), auth.username(), ChessGame.TeamColor.WHITE);
            return new CreateGameResult(newGame.gameID());
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during game creation", e);
        }
    }

    public JoinGameResult joinGame(String authToken, int gameID, String playerColor) {
        try {
            AuthData auth = authService.validateAuthToken(authToken);
            ChessGame.TeamColor color;

            try {
                color = ChessGame.TeamColor.valueOf(playerColor.toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException("Invalid team color: " + playerColor);
            }

            GameData game = gameDAO.getGame(gameID).orElseThrow(() -> new BadRequestException("Invalid game ID"));

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

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        authService.validateAuthToken(authToken);
        List<GameData> games = gameDAO.listGames();
        GameEntry[] entries = games.stream().map(game ->
                new GameEntry(game.gameID(),
                        game.gameName(),
                        game.whiteUsername(),
                        game.blackUsername())).toArray(GameEntry[]::new);
        return new ListGamesResult(entries);
    }

    public record CreateGameResult(int gameID) { }
    public record JoinGameResult() { }
    public record ListGamesResult(GameEntry[] games) { }
    public record GameEntry(int gameID, String gameName, String whiteUsername, String blackUsername) { }

    public record CreateGameRequest(String gameName) { }
    public record JoinGameRequest(String playerColor, int gameID) { }
}