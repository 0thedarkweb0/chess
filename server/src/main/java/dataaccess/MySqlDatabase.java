package dataaccess;

import java.sql.SQLException;
import java.sql.Statement;

public final class MySqlDatabase {
    private MySqlDatabase() {}

    private static final String CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) NOT NULL PRIMARY KEY,
                password_hash VARCHAR(60) NOT NULL,
                email VARCHAR(255) NOT NULL
            )
            """;

    private static final String CREATE_AUTH_TOKENS = """
            CREATE TABLE IF NOT EXISTS authToken (
                auth_token VARCHAR(255) NOT NULL PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                CONSTRAINT fk_auth_user
                    FOREIGN KEY (username) REFERENCES users(username)
            )
            """;

    private static final String CREATE_GAMES = """
            CREATE TABLE IF NOT EXISTS games (
                game_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                white_username VARCHAR(255) NULL,
                black_username VARCHAR(255) NULL,
                game_name VARCHAR(255) NOT NULL,
                game_json TEXT NOT NULL,
                CONSTRAINT fk_games_white_user
                    FOREIGN KEY (white_username) REFERENCES users(username),
                CONSTRAINT fk_games_black_user
                    FOREIGN KEY (black_username) REFERENCES users(username)
            )
            """;

    public static void initialize() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (var connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_USERS);
            statement.executeUpdate(CREATE_AUTH_TOKENS);
            statement.executeUpdate(CREATE_GAMES);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to initialize database tables", e);
        }
    }
}