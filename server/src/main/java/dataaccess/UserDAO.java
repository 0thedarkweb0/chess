package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public interface UserDAO {
    void clear();
    void createUser(UserData user);
    UserData getUser(String username);
}

