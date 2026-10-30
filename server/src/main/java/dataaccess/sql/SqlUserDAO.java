package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import model.UserData;

import java.sql.SQLException;

public class SqlUserDAO implements UserDAO {

    @Override
    public void clear() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement("DELETE FROM users")){
                statement.executeUpdate();
        }catch (SQLException e){
            throw new DataAccessException("failed to clear",e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException{
        String sql = """
                INSERT INTO users (username, password_hash, email)
                VALUES (?, ?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.email());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException{
        String sql = """
                SELECT username, password_hash, email
                FROM users
                WHERE username = ?
                """;
        try(var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(sql)){
            statement.setString(1,username);
            try(var result =statement.executeQuery()){
                if(result == null){
                    return null;
                }

                return new UserData(
                        result.getString("username"),
                        result.getString("password_hash"),
                        result.getString("email")
                );
            }

        }catch(SQLException e){
            throw new DataAccessException("Failed to get User",e);
        }
    }
}
