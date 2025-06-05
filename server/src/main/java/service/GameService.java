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
        AuthData auth = authService.validateAuthToken(authToken);
        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("Game name can't be null or empty");
        }
        GameData newGame;
        try {
            newGame = gameDAO.createGame(gameName);
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during game creation", e);
        }
        GameEntry entry = new GameEntry(newGame.gameID(), newGame.gameName(), "", "");
        return new CreateGameResult(true, newGame.gameID(), entry);
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
            game = gameDAO.getGame(gameID).orElseThrow(
                    () -> new BadRequestException("Invalid game ID"));
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error while retrieving game data", e);
        }

        if (color == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null && !game.whiteUsername().isBlank()) {
                throw new ForbiddenException("Someone is already playing as white!");
            }
            try {
                gameDAO.joinGame(gameID, auth.username(), ChessGame.TeamColor.WHITE);
            } catch (DataAccessException e) {
                throw new ServerException("Database connection error while joining as white", e);
            }
        } else {
            if (game.blackUsername() != null && !game.blackUsername().isBlank()) {
                throw new ForbiddenException("Someone is already playing as black!");
            }
            try {
                gameDAO.joinGame(gameID, auth.username(), ChessGame.TeamColor.BLACK);
            } catch (DataAccessException e) {
                throw new ServerException("Database connection error while joining as black", e);
            }
        }

        GameData updatedGame;
        try {
            updatedGame = gameDAO.getGame(gameID).orElseThrow(
                    () -> new ServerException("Game disappeared after join"));
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error after joining game", e);
        }

        GameEntry entry = new GameEntry(
                updatedGame.gameID(),
                updatedGame.gameName(),
                updatedGame.whiteUsername(),
                updatedGame.blackUsername()
        );
        return new JoinGameResult(true, entry);
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

    public ObserveGameResult observeGame(String authToken, int gameID) {
        AuthData auth = authService.validateAuthToken(authToken);

        GameData data;
        try {
            data = gameDAO.getGame(gameID).orElseThrow(
                    () -> new BadRequestException("Game ID: " + gameID + " does not exist."));
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error while trying to observe game", e);
        }

        ChessGame currentState = data.game();

        return new ObserveGameResult(true, currentState, null);
    }

    public record CreateGameResult(boolean success, Integer gameID, GameEntry game) {
    }
    public record JoinGameResult(boolean success, GameEntry game) { }
    public record ListGamesResult(boolean success, GameEntry[] games) { }
    public record ObserveGameResult(boolean success, ChessGame game, String message) { }

    public record GameEntry(int gameID, String gameName, String whiteUsername, String blackUsername) { }

    public record CreateGameRequest(String gameName) { }
    public record JoinGameRequest(int gameID, String playerColor) { }

}