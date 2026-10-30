package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.AuthData;

import java.sql.SQLException;

public class SqlAuthDAO implements AuthDAO {

    @Override
    public void clear() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("DELETE FROM authToken")){
            statement.executeUpdate();
        }catch (SQLException e){
            throw new DataAccessException("Failed to clear auth",e);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = """
                INSERT INTO authToken (auth_token, username)
                VALUES (?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create auth", e);
        }

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = """
                SELECT auth_token, username
                FROM authToken
                WHERE auth_token = ?
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, authToken);

            try (var result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return new AuthData(
                        result.getString("authToken"),
                        result.getString("username")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get auth token", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     "DELETE FROM authToken WHERE auth_token = ?")) {
            statement.setString(1, authToken);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth token", e);
        }
    }
}
