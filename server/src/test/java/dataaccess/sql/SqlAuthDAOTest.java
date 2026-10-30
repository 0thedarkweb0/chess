package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.sql.MySqlDatabase;
import dataaccess.sql.SqlAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlAuthDAOTest {
    private SqlAuthDAO authDAO;

    @BeforeEach
    void setUp() throws Exception {
        MySqlDatabase.initialize();
        authDAO = new SqlAuthDAO();

        // Clear child tables before users to respect the foreign keys.
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM games");
            statement.executeUpdate("DELETE FROM authToken");
            statement.executeUpdate("DELETE FROM users");
        }
    }

    @Test
    void createAuthStoresRowInDatabase() throws Exception {
        insertUserDirectly("alice");
        var auth = new AuthData("token-1", "alice");

        authDAO.createAuth(auth);

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT auth_token, username
                     FROM authToken
                     WHERE auth_token = ?
                     """)) {
            statement.setString(1, "token-1");

            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals("token-1", result.getString("auth_token"));
                assertEquals("alice", result.getString("username"));
                assertFalse(result.next());
            }
        }
    }

    @Test
    void getAuthReadsExistingDatabaseRow() throws Exception {
        insertUserDirectly("alice");
        insertAuthDirectly("token-1", "alice");

        assertEquals(
                new AuthData("token-1", "alice"),
                authDAO.getAuth("token-1")
        );
    }

    @Test
    void missingAuthReturnsNull() throws DataAccessException {
        assertNull(authDAO.getAuth("missing-token"));
    }

    @Test
    void deleteAuthRemovesDatabaseRow() throws Exception {
        insertUserDirectly("alice");
        authDAO.createAuth(new AuthData("token-1", "alice"));

        authDAO.deleteAuth("token-1");

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM authToken
                     WHERE auth_token = ?
                     """)) {
            statement.setString(1, "token-1");

            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(0, result.getInt(1));
            }
        }
    }

    @Test
    void clearDeletesAllAuthRows() throws Exception {
        insertUserDirectly("alice");
        insertUserDirectly("bob");
        authDAO.createAuth(new AuthData("token-1", "alice"));
        authDAO.createAuth(new AuthData("token-2", "bob"));

        authDAO.clear();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM authToken")) {
            assertTrue(result.next());
            assertEquals(0, result.getInt(1));
        }
    }

    @Test
    void duplicateTokenIsRejected() throws Exception {
        insertUserDirectly("alice");
        var auth = new AuthData("token-1", "alice");
        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth));
    }

    private void insertUserDirectly(String username) throws Exception {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     INSERT INTO users (username, password_hash, email)
                     VALUES (?, ?, ?)
                     """)) {
            statement.setString(1, username);
            statement.setString(2, "test-hash");
            statement.setString(3, username + "@example.com");
            statement.executeUpdate();
        }
    }

    private void insertAuthDirectly(String token, String username) throws Exception {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     INSERT INTO authToken (auth_token, username)
                     VALUES (?, ?)
                     """)) {
            statement.setString(1, token);
            statement.setString(2, username);
            statement.executeUpdate();
        }
    }
}