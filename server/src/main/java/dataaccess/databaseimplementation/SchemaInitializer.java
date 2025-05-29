package dataaccess.databaseimplementation;

import dataaccess.DatabaseManager;

import java.io.*;
import java.sql.*;
import java.util.Objects;

public class SchemaInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection();
             BufferedReader bufferedReader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(
                        SchemaInitializer.class.getClassLoader().getResourceAsStream("chess.sql")))
                );
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            for (String sql : sb.toString().split(";")) {
                if (!sql.isBlank()) {
                    stmt.executeUpdate(sql.strip());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize the schema", e);
        }
    }

}
