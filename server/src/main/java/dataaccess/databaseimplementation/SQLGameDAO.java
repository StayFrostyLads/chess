package dataaccess.databaseimplementation;

import chess.ChessGame;
import dataaccess.*;
import json.JsonUtil;
import model.GameData;
import service.*;

import java.sql.*;
import java.util.*;

public class SQLGameDAO implements GameDAO {

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear the games table", e);
        }
    }

    @Override
    public void addGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (gameID, gameName, whiteUsername, blackUsername, gameState) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, game.gameID());
            stmt.setString(2, game.gameName());
            stmt.setString(3, game.whiteUsername());
            stmt.setString(4, game.blackUsername());
            stmt.setString(5, JsonUtil.toJson(game.game()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error adding game", e);
        }
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        String sql = "INSERT INTO games (gameName, gameState) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ChessGame newGame = new ChessGame();
            stmt.setString(1, gameName);
            stmt.setString(2, JsonUtil.toJson(newGame));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int gameID = rs.getInt(1);
                    return new GameData(gameID, null, null, gameName, newGame);
                } else {
                    throw new DataAccessException("Failed to retrieve generated gameID.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game", e);
        }
    }

    @Override
    public Optional<GameData> getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            JsonUtil.fromJson(rs.getString("gameState"), ChessGame.class)
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching the game", e);
        }
        return Optional.empty();
    }

    @Override
    public void joinGame(int gameID, String username, ChessGame.TeamColor color) throws DataAccessException {
        final String idSQL = "SELECT gameID FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement idStmt = conn.prepareStatement(idSQL)) {

            idStmt.setInt(1, gameID);
            try (ResultSet rs = idStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("Invalid game ID");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Database connection error while validating game ID", e);
        }

        final String fetchSql = "SELECT whiteUsername, blackUsername FROM games WHERE gameID = ?";
        String white, black;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {

            fetchStmt.setInt(1, gameID);
            try (ResultSet rs = fetchStmt.executeQuery()) {
                // We already know the game exists, so rs.next() must be true
                rs.next();
                white = rs.getString("whiteUsername");
                black = rs.getString("blackUsername");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection error while reading game data", e);
        }

        if (color == ChessGame.TeamColor.WHITE) {
            if (white != null) {
                throw new ForbiddenException("Color already taken by another player!");
            }

            final String updateWhiteSql = "UPDATE games SET whiteUsername = ? WHERE gameID = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement updateStmt = conn.prepareStatement(updateWhiteSql)) {

                updateStmt.setString(1, username);
                updateStmt.setInt(2, gameID);
                updateStmt.executeUpdate();

            } catch (SQLException e) {
                throw new DataAccessException("Database connection error while joining as white user", e);
            }

        } else {
            if (black != null) {
                throw new ForbiddenException("Color already taken by another player!");
            }

            final String updateBlackSql = "UPDATE games SET blackUsername = ? WHERE gameID = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement updateStmt = conn.prepareStatement(updateBlackSql)) {

                updateStmt.setString(1, username);
                updateStmt.setInt(2, gameID);
                updateStmt.executeUpdate();

            } catch (SQLException e) {
                throw new DataAccessException("Database connection error while joining as black user", e);
            }
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT * FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        JsonUtil.fromJson(rs.getString("gameState"), ChessGame.class)
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing the games", e);
        }

        return games;
    }




}