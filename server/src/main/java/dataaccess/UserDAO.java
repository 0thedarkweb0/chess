package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public interface UserDAO {
    void clear() throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
}

