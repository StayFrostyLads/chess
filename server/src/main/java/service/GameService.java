package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;

import java.util.Collections;
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
            return new CreateGameResult(newGame.gameID());
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during game creation", e);
        }
    }

    public JoinGameResult joinGame(String authToken, int gameID, String playerColor) {
        AuthData auth = authService.validateAuthToken(authToken);
        ChessGame.TeamColor color;

        try {
            color = ChessGame.TeamColor.valueOf(playerColor.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid team color: " + playerColor);
        }

        GameData game;
        try {
            game = gameDAO.getGame(gameID).orElseThrow(() -> new BadRequestException("Invalid game ID"));
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error while retrieving game data", e);
        }

        if (color == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new ForbiddenException("Someone is already playing as white!");
            }
            try {
                gameDAO.joinGame(gameID, auth.username(), ChessGame.TeamColor.WHITE);
                return new JoinGameResult();
            } catch (DataAccessException e) {
                throw new ServerException("Database connection error while joining as white", e);
            }
        }

        if (color == ChessGame.TeamColor.BLACK) {
            if (game.blackUsername() != null) {
                throw new ForbiddenException("Someone is already playing as black!");
            }
            try {
                gameDAO.joinGame(gameID, auth.username(), ChessGame.TeamColor.BLACK);
                return new JoinGameResult();
            } catch (DataAccessException e) {
                throw new ServerException("Database connection error while joining as black", e);
            }
        }

        throw new BadRequestException("Invalid team color: " + playerColor);
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        authService.validateAuthToken(authToken);
        List<GameData> listedGames = gameDAO.listGames();
        if (listedGames == null) {
            listedGames = Collections.emptyList();
        }
        GameEntry[] games = listedGames.stream().map(game ->
                new GameEntry(game.gameID(),
                        game.gameName(),
                        game.whiteUsername(),
                        game.blackUsername())).toArray(GameEntry[]::new);
        return new ListGamesResult(true, games);
    }

    public record CreateGameResult(int gameID) { }
    public record JoinGameResult() { }
    public record ListGamesResult(boolean success, GameEntry[] games) { }
    public record GameEntry(int gameID, String gameName, String whiteUsername, String blackUsername) { }

    public record CreateGameRequest(String gameName) { }
    public record JoinGameRequest(int gameID, String playerColor) { }
}