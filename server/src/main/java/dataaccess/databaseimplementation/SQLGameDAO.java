package dataaccess.databaseimplementation;

import chess.ChessGame;
import dataaccess.*;
import json.JsonUtil;
import model.GameData;

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
    public int createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (gameName, gameState) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, game.gameName());
            stmt.setString(2, JsonUtil.toJson(game.game()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
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
                            rs.getString("gameName"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
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
        String sql = switch (color) {
            case WHITE -> "UPDATE games SET whiteUsername = ? WHERE gameID = ?";
            case BLACK -> "UPDATE games SET blackUsername = ? WHERE gameID = ?";
        };

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, gameID);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("No game found with ID " + gameID);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error joining game", e);
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
                        rs.getString("gameName"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        JsonUtil.fromJson(rs.getString("gameState"), ChessGame.class)
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing the games", e);
        }

        return games;
    }




}
