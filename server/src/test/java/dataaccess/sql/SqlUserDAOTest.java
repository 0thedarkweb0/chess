package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dataaccess.sql.SqlTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

class SqlUserDAOTest {
    private SqlUserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        resetTables();
    }

    @Test
    void createUserWritesRowToDatabase() throws Exception {
        insertUser("alice");

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT username, password_hash, email
                     FROM users
                     WHERE username = ?
                     """)) {
            statement.setString(1, "alice");

            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals("alice", result.getString("username"));
                assertEquals("test-hash", result.getString("password_hash"));
                assertEquals("alice@example.com", result.getString("email"));
                assertFalse(result.next());
            }
        }
    }

    @Test
    void getUserReadsExistingDatabaseRow() throws Exception {
        var expected = new UserData("alice", "test-hash", "alice@example.com");

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     INSERT INTO users (username, password_hash, email)
                     VALUES (?, ?, ?)
                     """)) {
            statement.setString(1, expected.username());
            statement.setString(2, expected.password());
            statement.setString(3, expected.email());
            statement.executeUpdate();
        }

        assertEquals(expected, userDAO.getUser("alice"));
    }

    @Test
    void missingUserReturnsNull() throws DataAccessException {
        assertNull(userDAO.getUser("nobody"));
    }

    @Test
    void clearDeletesRowsFromDatabase() throws Exception {
        insertUser("alice");
        userDAO.clear();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            assertTrue(result.next());
            assertEquals(0, result.getInt(1));
        }
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        var user = new UserData("alice", "test-hash", "alice@example.com");
        userDAO.createUser(user);

        assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
    }
}