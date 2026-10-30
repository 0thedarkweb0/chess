package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClearServiceTest {
    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;
    private ClearService service;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        service = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    void clearRemovesUsersAuthTokensAndGames() throws DataAccessException {
        userDAO.createUser(new UserData("alice", "password", "alice@example.com"));
        authDAO.createAuth(new AuthData("token", "alice"));
        int gameID = gameDAO.createGame("opening game");

        service.clear();

        assertNull(userDAO.getUser("alice"));
        assertNull(authDAO.getAuth("token"));
        assertTrue(gameDAO.listGames().isEmpty());
        assertNull(gameDAO.getGame(gameID));
    }
}
