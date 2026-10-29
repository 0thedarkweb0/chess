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
    }

    @Override
    public UserData getUser(String username) throws DataAccessException{
        return null;
    }
}
