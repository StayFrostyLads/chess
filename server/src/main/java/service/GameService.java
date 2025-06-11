package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.*;
import model.*;

import java.util.Collections;
import java.util.List;

import static chess.ChessGame.TeamColor.*;

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

        if (color == WHITE) {
            if (game.whiteUsername() != null && !game.whiteUsername().isBlank()) {
                throw new ForbiddenException("Someone is already playing as white!");
            }
            try {
                gameDAO.joinGame(gameID, auth.username(), WHITE);
            } catch (DataAccessException e) {
                throw new ServerException("Database connection error while joining as white", e);
            }
        } else {
            if (game.blackUsername() != null && !game.blackUsername().isBlank()) {
                throw new ForbiddenException("Someone is already playing as black!");
            }
            try {
                gameDAO.joinGame(gameID, auth.username(), BLACK);
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
        String username = auth.username();

        GameData data;
        try {
            data = gameDAO.getGame(gameID).orElseThrow(
                    () -> new BadRequestException("Game ID: " + gameID + " does not exist."));
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error while trying to observe game", e);
        }

        ChessGame.TeamColor role;
        if (username.equals(data.whiteUsername())) {
            role = WHITE;
        } else if (username.equals(data.blackUsername())) {
            role = BLACK;
        } else {
            role = null; // observer
        }

        ChessGame currentState = data.game();

        return new ObserveGameResult(true, currentState, null, role);
    }

    public MakeMoveResult makeMove(String authToken, int gameID, ChessMove move) throws DataAccessException {
        AuthData auth = authService.validateAuthToken(authToken);
        String username = auth.username();

        GameData gameData = gameDAO.getGame(gameID).orElseThrow(
                () -> new BadRequestException("Game ID " + gameID + " does not exist"));

        ChessGame game = gameData.game();

        ChessGame.TeamColor playerColor;
        if (username.equals(gameData.whiteUsername())) {
            playerColor = WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            playerColor = BLACK;
        } else {
            throw new ForbiddenException("Observers may not make a move!");
        }

        if (game.isInCheckmate(WHITE) || game.isInCheckmate(BLACK) ||
                game.isInStalemate(WHITE) || game.isInStalemate(BLACK)) {
            return new MakeMoveResult(false, game, null, false,
                        false, "Game is already over!");
        }

        if (game.getTeamTurn() != playerColor) {
            return new MakeMoveResult(false, game, null, false,
                        false, "It is not currently " + playerColor + "'s turn to move!");
        }

        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            return new MakeMoveResult(false, game, null, false,
                        false, "Illegal move: " + e.getMessage());
        }

        try {
            gameDAO.saveGame(gameID, game);
        } catch (DataAccessException e) {
            throw new ServerException("Failed to save move", e);
        }

        String notification = String.format("%s moved %s to %s", username,
                                            move.getStartPosition().toAlgebraic(),
                                            move.getEndPosition().toAlgebraic());

        ChessGame.TeamColor opponent = playerColor.other();
        boolean check = game.isInCheck(opponent);
        boolean checkmate = game.isInCheckmate(opponent);

        return new MakeMoveResult(true, game, notification, check, checkmate, null);
    }

    public record CreateGameRequest(String gameName) { }
    public record CreateGameResult(boolean success, Integer gameID, GameEntry game) { }

    public record JoinGameRequest(int gameID, String playerColor) { }
    public record JoinGameResult(boolean success, GameEntry game) { }

    public record ListGamesResult(boolean success, GameEntry[] games) { }

    public record ObserveGameResult(boolean success, ChessGame game, String message, ChessGame.TeamColor playerRole) { }

    public record MakeMoveRequest(String authToken, int gameID, ChessMove move) { }
    public record MakeMoveResult(boolean success, ChessGame game, String notification,
                                 boolean isCheck, boolean isCheckmate, String message) { }
    public record GameEntry(int gameID, String gameName, String whiteUsername, String blackUsername) { }


}