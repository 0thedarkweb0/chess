package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import model.GameData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    @Override
    public void clear() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM games");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear games", e);
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        String sql = """
                INSERT INTO games (white_username, black_username, game_name, game_json)
                VALUES (NULL, NULL, ?, ?)
                """;

        String gameJson = gson.toJson(new ChessGame());

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, gameName);
            statement.setString(2, gameJson);
            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            throw new DataAccessException("Creating game did not return an ID");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = """
                SELECT game_id, white_username, black_username, game_name, game_json
                FROM games
                WHERE game_id = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, gameID);

            try (var result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                ChessGame game = gson.fromJson(
                        result.getString("game_json"), ChessGame.class);

                return new GameData(
                        result.getInt("game_id"),
                        result.getString("white_username"),
                        result.getString("black_username"),
                        result.getString("game_name"),
                        game
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String sql = """
                SELECT game_id, white_username, black_username, game_name, game_json
                FROM games
                ORDER BY game_id
                """;

        List<GameData> games = new ArrayList<>();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);
             var result = statement.executeQuery()) {
            while (result.next()) {
                ChessGame game = gson.fromJson(
                        result.getString("game_json"), ChessGame.class);

                games.add(new GameData(
                        result.getInt("game_id"),
                        result.getString("white_username"),
                        result.getString("black_username"),
                        result.getString("game_name"),
                        game
                ));
            }
            return games;
        } catch (SQLException | JsonParseException e) {
            throw new DataAccessException("Failed to list games", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = """
                UPDATE games
                SET white_username = ?, black_username = ?, game_name = ?, game_json = ?
                WHERE game_id = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));
            statement.setInt(5, game.gameID());

            if (statement.executeUpdate() == 0) {
                throw new DataAccessException("Game does not exist");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }
    }
}