package service;

import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.records.LoginRequest;
import service.records.LoginResult;
import service.records.RegisterRequest;
import service.records.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private MemoryUserDAO userDAO;
    private MemoryAuthDAO authDAO;
    private UserService service;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        service = new UserService(userDAO, authDAO);
    }

    @Test
    void registerCreatesUserAndAuthToken() throws Exception {
        RegisterResult result = service.register(
                new RegisterRequest("alice", "password", "alice@example.com"));

        assertEquals("alice", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
        assertEquals(new UserData("alice", "password", "alice@example.com"),
                userDAO.getUser("alice"));
        assertEquals(new AuthData(result.authToken(), "alice"),
                authDAO.getAuth(result.authToken()));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        userDAO.createUser(new UserData("alice", "old-password", "alice@example.com"));

        assertThrows(AlreadyTakenException.class, () -> service.register(
                new RegisterRequest("alice", "password", "new@example.com")));
        assertEquals("old-password", userDAO.getUser("alice").password());
    }

    @Test
    void registerRejectsNullRequest() {
        assertThrows(BadRequestException.class, () -> service.register(null));
    }

    @Test
    void registerRejectsMissingUsername() {
        assertThrows(BadRequestException.class, () -> service.register(
                new RegisterRequest(null, "password", "alice@example.com")));
    }

    @Test
    void registerRejectsBlankPassword() {
        assertThrows(BadRequestException.class, () -> service.register(
                new RegisterRequest("alice", "  ", "alice@example.com")));
    }

    @Test
    void registerRejectsMissingEmail() {
        assertThrows(BadRequestException.class, () -> service.register(
                new RegisterRequest("alice", "password", null)));
    }

    @Test
    void loginCreatesNewAuthTokenForValidCredentials() throws Exception {
        userDAO.createUser(new UserData("alice", "password", "alice@example.com"));

        LoginResult result = service.login(new LoginRequest("alice", "password"));

        assertEquals("alice", result.username());
        assertNotNull(result.authToken());
        assertEquals(new AuthData(result.authToken(), "alice"),
                authDAO.getAuth(result.authToken()));
    }

    @Test
    void loginRejectsUnknownUser() {
        assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("missing", "password")));
    }

    @Test
    void loginRejectsWrongPassword() {
        userDAO.createUser(new UserData("alice", "password", "alice@example.com"));

        assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("alice", "wrong")));
    }

    @Test
    void loginRejectsNullRequest() {
        assertThrows(BadRequestException.class, () -> service.login(null));
    }

    @Test
    void loginRejectsBlankCredentials() {
        assertThrows(BadRequestException.class,
                () -> service.login(new LoginRequest("alice", " ")));
        assertThrows(BadRequestException.class,
                () -> service.login(new LoginRequest(null, "password")));
    }

    @Test
    void logoutDeletesExistingAuthToken() throws DataAccessException {
        authDAO.createAuth(new AuthData("token", "alice"));

        service.logout("token");

        assertNull(authDAO.getAuth("token"));
    }

    @Test
    void logoutRejectsNullOrUnknownToken() {
        assertThrows(UnauthorizedException.class, () -> service.logout(null));
        assertThrows(UnauthorizedException.class, () -> service.logout("unknown"));
    }
    @Test
    void registerRejectsMissingPassword() {
        assertThrows(BadRequestException.class, () -> service.register(
                new RegisterRequest("alice", null, "alice@example.com")));
    }

    @Test
    void registerRejectsBlankUsernameAndEmail() {
        assertThrows(BadRequestException.class, () -> service.register(
                new RegisterRequest("  ", "password", "alice@example.com")));
        assertThrows(BadRequestException.class, () -> service.register(
                new RegisterRequest("alice", "password", "  ")));
    }

    @Test
    void loginRejectsMissingPassword() {
        assertThrows(BadRequestException.class,
                () -> service.login(new LoginRequest("alice", null)));
    }

}
