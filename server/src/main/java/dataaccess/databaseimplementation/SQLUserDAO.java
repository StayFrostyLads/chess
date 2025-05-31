package dataaccess.databaseimplementation;

import dataaccess.*;
import model.UserData;

import java.sql.*;
import java.util.*;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() {
        try (var conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(60) NOT NULL,
                    email VARCHAR(100) NOT NULL
                )
            """);

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to create the users table", e);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users");
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to clear the users table", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());
            stmt.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DataAccessException("User already exists in the database!", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting the new user", e);
        }
    }

    @Override
    public Optional<UserData> getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    ));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving the specified user", e);
        }
    }

    @Override
    public List<UserData> findAll() {
        List<UserData> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error retrieving user data for all users", e);
        }

        return users;
    }


}
