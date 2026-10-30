package dataaccess.sql;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.MySqlDatabase;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class SqlGameDAOTest {
    private final Gson gson = new Gson();
    private SqlGameDAO gameDAO;

    @BeforeEach
    void setUp() throws Exception {
        MySqlDatabase.initialize();
        gameDAO = new SqlGameDAO();

        // Clear dependent rows first to respect the foreign keys.
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM games");
            statement.executeUpdate("DELETE FROM authToken");
            statement.executeUpdate("DELETE FROM users");
        }
    }

    @Test
    void createGameStoresInitialGameAndReturnsGeneratedId() throws Exception {
        int gameID = gameDAO.createGame("opening");

        assertTrue(gameID > 0);

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT white_username, black_username, game_name, game_json
                     FROM games
                     WHERE game_id = ?
                     """)) {
            statement.setInt(1, gameID);

            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                assertNull(result.getString("white_username"));
                assertNull(result.getString("black_username"));
                assertEquals("opening", result.getString("game_name"));

                var storedGame = gson.fromJson(
                        result.getString("game_json"), ChessGame.class);
                assertEquals(new ChessGame(), storedGame);
                assertFalse(result.next());
            }
        }
    }

    @Test
    void getGameReadsExistingDatabaseRow() throws Exception {
        var expectedGame = new ChessGame();
        int gameID = insertGameDirectly("seeded game", expectedGame);

        GameData actual = gameDAO.getGame(gameID);

        assertEquals(
                new GameData(gameID, null, null, "seeded game", expectedGame),
                actual
        );
    }

    @Test
    void getMissingGameReturnsNull() throws DataAccessException {
        assertNull(gameDAO.getGame(Integer.MAX_VALUE));
    }

    @Test
    void listGamesReadsAllDatabaseRows() throws Exception {
        int firstID = insertGameDirectly("first", new ChessGame());

        var secondGame = new ChessGame();
        secondGame.setTeamTurn(ChessGame.TeamColor.BLACK);
        int secondID = insertGameDirectly("second", secondGame);

        var games = gameDAO.listGames();

        assertEquals(2, games.size());
        assertEquals(firstID, games.get(0).gameID());
        assertEquals("first", games.get(0).gameName());
        assertEquals(secondID, games.get(1).gameID());
        assertEquals("second", games.get(1).gameName());
        assertEquals(
                ChessGame.TeamColor.BLACK,
                games.get(1).game().getTeamTurn()
        );
    }

    @Test
    void updateGamePersistsPlayersAndMove() throws Exception {
        new SqlUserDAO().createUser(
                new UserData("alice", "test-hash", "alice@example.com")
        );
        new SqlUserDAO().createUser(
                new UserData("bob", "test-hash", "bob@example.com")
        );        int gameID = gameDAO.createGame("before");

        var updatedGame = new ChessGame();
        updatedGame.makeMove(new ChessMove(
                new ChessPosition(2, 5),
                new ChessPosition(4, 5),
                null
        ));

        gameDAO.updateGame(
                new GameData(gameID, "alice", "bob", "after", updatedGame));

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT white_username, black_username, game_name, game_json
                     FROM games
                     WHERE game_id = ?
                     """)) {
            statement.setInt(1, gameID);

            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals("alice", result.getString("white_username"));
                assertEquals("bob", result.getString("black_username"));
                assertEquals("after", result.getString("game_name"));

                var storedGame = gson.fromJson(
                        result.getString("game_json"), ChessGame.class);

                assertEquals(
                        ChessGame.TeamColor.BLACK,
                        storedGame.getTeamTurn()
                );
                assertEquals(
                        new ChessPiece(
                                ChessGame.TeamColor.WHITE,
                                ChessPiece.PieceType.PAWN
                        ),
                        storedGame.getBoard().getPiece(new ChessPosition(4, 5))
                );
            }
        }
    }

    @Test
    void updateMissingGameThrowsDataAccessException() {
        assertThrows(DataAccessException.class, () ->
                gameDAO.updateGame(new GameData(
                        Integer.MAX_VALUE, null, null, "missing", new ChessGame())));
    }

    @Test
    void clearDeletesDatabaseRows() throws Exception {
        insertGameDirectly("to delete", new ChessGame());

        gameDAO.clear();

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM games")) {
            assertTrue(result.next());
            assertEquals(0, result.getInt(1));
        }
    }

    private int insertGameDirectly(String name, ChessGame game) throws Exception {
        String sql = """
                INSERT INTO games
                    (white_username, black_username, game_name, game_json)
                VALUES (NULL, NULL, ?, ?)
                """;

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, gson.toJson(game));
            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                return keys.getInt(1);
            }
        }
    }

}