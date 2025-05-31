package dataaccess.databaseimplementation;

import dataaccess.*;
import model.AuthData;

import java.sql.*;
import java.util.*;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() {
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(100) PRIMARY KEY,
                    username VARCHAR(50) NOT NULL,
                    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
            )
            """);

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to create auth table", e);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM auth");
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to clear auth table", e);
        }
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            stmt.setString(2, username);
            stmt.executeUpdate();

            return new AuthData(authToken, username);

        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth token", e);
        }
    }

    @Override
    public Optional<AuthData> getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT * FROM auth WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    ));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error accessing the auth token", e);
        }
    }

    @Override
    public List<AuthData> findAll() {
        List<AuthData> authList = new ArrayList<>();
        String sql = "SELECT * FROM auth";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                authList.add(new AuthData(
                        rs.getString("authToken"),
                        rs.getString("username")
                ));
            }

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error retrieving all auth tokens", e);
        }

        return authList;
    }

    @Override
    public boolean removeToken(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authToken);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Error removing the auth token", e);
        }
    }
}
