package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.records.*;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    private static final String TOKEN = "valid-token";
    private static final String USERNAME = "alice";

    private MemoryAuthDAO authDAO;
    private MemoryGameDAO gameDAO;
    private GameService service;

    @BeforeEach
    void setUp() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        authDAO.createAuth(new AuthData(TOKEN, USERNAME));
        service = new GameService(authDAO, gameDAO);
    }

    @Test
    void listGamesReturnsEmptyListWhenNoGamesExist() {
        ListGameResult result = service.listGames(TOKEN);

        assertNotNull(result.games());
        assertTrue(result.games().isEmpty());
    }

    @Test
    void listGamesReturnsGameDetails() {
        int gameID = gameDAO.createGame("opening");

        ListGameResult result = service.listGames(TOKEN);

        assertEquals(1, result.games().size());
        GameListItem listed = result.games().get(0);
        assertEquals(gameID, listed.gameID());
        assertNull(listed.whiteUsername());
        assertNull(listed.blackUsername());
        assertEquals("opening", listed.gameName());
    }

    @Test
    void listGamesRejectsInvalidToken() {
        assertThrows(UnauthorizedException.class, () -> service.listGames("bad-token"));
        assertThrows(UnauthorizedException.class, () -> service.listGames(null));
    }

    @Test
    void createGameStoresGameAndReturnsItsID() throws Exception {
        CreateGameResult result = service.createGame(TOKEN, new CreateGameRequest("opening"));

        assertTrue(result.gameID() > 0);
        GameData stored = gameDAO.getGame(result.gameID());
        assertNotNull(stored);
        assertEquals("opening", stored.gameName());
        assertNotNull(stored.game());
    }

    @Test
    void createGameRejectsMissingOrBlankName() {
        assertThrows(BadRequestException.class,
                () -> service.createGame(TOKEN, new CreateGameRequest(null)));
        assertThrows(BadRequestException.class,
                () -> service.createGame(TOKEN, new CreateGameRequest("  ")));
        assertThrows(BadRequestException.class, () -> service.createGame(TOKEN, null));
    }

    @Test
    void createGameRejectsInvalidToken() {
        assertThrows(UnauthorizedException.class,
                () -> service.createGame("bad-token", new CreateGameRequest("opening")));
    }

    @Test
    void joinGameClaimsWhite() throws DataAccessException, BadRequestException, AlreadyTakenException {
        int gameID = gameDAO.createGame("opening");

        service.joinGame(TOKEN, new JoinGameRequest("WHITE", gameID));

        assertEquals(USERNAME, gameDAO.getGame(gameID).whiteUsername());
        assertNull(gameDAO.getGame(gameID).blackUsername());
    }

    @Test
    void joinGameClaimsBlack() throws DataAccessException, BadRequestException, AlreadyTakenException {
        int gameID = gameDAO.createGame("opening");

        service.joinGame(TOKEN, new JoinGameRequest("BLACK", gameID));

        assertNull(gameDAO.getGame(gameID).whiteUsername());
        assertEquals(USERNAME, gameDAO.getGame(gameID).blackUsername());
    }

    @Test
    void joinGameRejectsInvalidToken() {
        assertThrows(UnauthorizedException.class,
                () -> service.joinGame("bad-token", new JoinGameRequest("WHITE", 1)));
    }

    @Test
    void joinGameRejectsInvalidColor() {
        assertThrows(BadRequestException.class,
                () -> service.joinGame(TOKEN, null));
        assertThrows(BadRequestException.class,
                () -> service.joinGame(TOKEN, new JoinGameRequest("GREEN", 1)));
    }

    @Test
    void joinGameRejectsTakenColor() throws DataAccessException {
        int gameID = gameDAO.createGame("opening");
        gameDAO.updateGame(new GameData(gameID, "bob", null, "opening", new ChessGame()));

        assertThrows(AlreadyTakenException.class,
                () -> service.joinGame(TOKEN, new JoinGameRequest("WHITE", gameID)));
    }

}
