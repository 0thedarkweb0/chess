package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.MySqlDatabase;
import model.UserData;

import java.sql.SQLException;
import java.util.Set;

final class SqlTestHelper {
    private SqlTestHelper() {}

    static void resetTables() throws Exception {
        MySqlDatabase.initialize();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM games");
            statement.executeUpdate("DELETE FROM authToken");
            statement.executeUpdate("DELETE FROM users");
        }
    }

    static void insertUser(String username) throws DataAccessException {
        new SqlUserDAO().createUser(
                new UserData(
                        username,
                        "test-hash",
                        username + "@example.com"
                )
        );
    }

    static int countRows(String tableName)
            throws DataAccessException, SQLException {
        if (!Set.of("users", "authToken", "games").contains(tableName)) {
            throw new IllegalArgumentException("Unexpected table: " + tableName);
        }

        String sql = "SELECT COUNT(*) FROM " + tableName;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }
}