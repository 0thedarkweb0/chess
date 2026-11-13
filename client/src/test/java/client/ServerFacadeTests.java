package client;

import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/db"))
                .DELETE()
                .build();

        var response = HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode(), "Could not clear the test database");
    }


    @Test
    void registerSucceeds() throws IOException, InterruptedException {
        AuthData result = facade.register("player1", "password", "p1@example.com");

        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }

    @Test
    void registerRejectsDuplicateUsername() throws IOException, InterruptedException {
        facade.register("player1", "password", "p1@example.com");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.register("player1", "another-password", "other@example.com")
        );

        assertEquals("That username is already taken.", error.getMessage());
    }

    @Test
    void registerRejectsMissingInformation() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.register("", "password", "p1@example.com")
        );

        assertEquals("Enter a username, password, and email.", error.getMessage());
    }

    @Test
    void loginSucceeds() throws IOException, InterruptedException {
        facade.register("player1", "password", "p1@example.com");

        AuthData result = facade.login("player1", "password");

        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }

    @Test
    void loginRejectsIncorrectPassword() throws IOException, InterruptedException {
        facade.register("player1", "password", "p1@example.com");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.login("player1", "wrong-password")
        );

        assertEquals("Username or password is incorrect.", error.getMessage());
    }

    @Test
    void loginRejectsMissingInformation() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.login("", "")
        );

        assertEquals("Enter your username and password.", error.getMessage());
    }
    @Test
    void logoutSucceeds() throws IOException, InterruptedException {
        AuthData player = registerPlayer("player1");

        assertDoesNotThrow(() -> facade.logout(player.authToken()));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.listGame(player.authToken())
        );
        assertEquals(
                "Your session is invalid. Please log in again.",
                error.getMessage()
        );
    }

    @Test
    void logoutRejectsInvalidToken() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.logout("not-a-valid-token")
        );

        assertEquals(
                "Your session is no longer valid. Please log in again.",
                error.getMessage()
        );
    }

    @Test
    void createGameSucceeds() throws IOException, InterruptedException {
        AuthData player = registerPlayer("player1");

        facade.createGame(player.authToken(), "Friday match");

        List<ServerFacade.GameInfo> games =
                facade.listGame(player.authToken());

        assertEquals(1, games.size());
        assertEquals("Friday match", games.getFirst().gameName());
    }

    @Test
    void createGameRejectsBlankName()
            throws IOException, InterruptedException {
        AuthData player = registerPlayer("player1");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.createGame(player.authToken(), "")
        );

        assertEquals("Enter a valid game name.", error.getMessage());
    }

    @Test
    void createGameRejectsInvalidToken() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.createGame("not-a-valid-token", "Friday match")
        );

        assertEquals(
                "Your session is invalid. Please login again",
                error.getMessage()
        );
    }

    @Test
    void listGameSucceeds() throws IOException, InterruptedException {
        AuthData player = registerPlayer("player1");
        facade.createGame(player.authToken(), "Friday match");

        List<ServerFacade.GameInfo> games =
                facade.listGame(player.authToken());

        assertEquals(1, games.size());
        assertEquals("Friday match", games.getFirst().gameName());
        assertNull(games.getFirst().whiteUsername());
        assertNull(games.getFirst().blackUsername());
    }

    @Test
    void listGameReturnsEmptyList() throws IOException, InterruptedException {
        AuthData player = registerPlayer("player1");

        List<ServerFacade.GameInfo> games =
                facade.listGame(player.authToken());

        assertTrue(games.isEmpty());
    }

    @Test
    void listGameRejectsInvalidToken() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.listGame("not-a-valid-token")
        );

        assertEquals(
                "Your session is invalid. Please log in again.",
                error.getMessage()
        );
    }

    @Test
    void joinGameSucceeds() throws IOException, InterruptedException {
        AuthData player = registerPlayer("player1");
        int gameID = createGameAndGetId(player.authToken(), "Friday match");

        assertDoesNotThrow(
                () -> facade.joinGame(player.authToken(), gameID, "WHITE")
        );

        List<ServerFacade.GameInfo> games =
                facade.listGame(player.authToken());

        assertEquals("player1", games.getFirst().whiteUsername());
    }

    @Test
    void joinGameRejectsInvalidColor()
            throws IOException, InterruptedException {
        AuthData player = registerPlayer("player1");
        int gameID = createGameAndGetId(player.authToken(), "Friday match");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.joinGame(player.authToken(), gameID, "PURPLE")
        );

        assertEquals("Choose a valid game and color.", error.getMessage());
    }

    @Test
    void joinGameRejectsInvalidGameID()
            throws IOException, InterruptedException {
        AuthData player = registerPlayer("player1");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.joinGame(player.authToken(), -1, "WHITE")
        );

        assertEquals("Choose a valid game and color.", error.getMessage());
    }

    @Test
    void joinGameRejectsInvalidToken()
            throws IOException, InterruptedException {
        AuthData player = registerPlayer("player1");
        int gameID = createGameAndGetId(player.authToken(), "Friday match");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.joinGame("not-a-valid-token", gameID, "WHITE")
        );

        assertEquals(
                "Your session is invalid. Please log in again.",
                error.getMessage()
        );
    }

    @Test
    void joinGameRejectsTakenColor()
            throws IOException, InterruptedException {
        AuthData firstPlayer = registerPlayer("player1");
        AuthData secondPlayer = registerPlayer("player2");
        int gameID = createGameAndGetId(firstPlayer.authToken(), "Friday match");

        facade.joinGame(firstPlayer.authToken(), gameID, "WHITE");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> facade.joinGame(secondPlayer.authToken(), gameID, "WHITE")
        );

        assertEquals("That color is already taken.", error.getMessage());
    }

    private AuthData registerPlayer(String username)
            throws IOException, InterruptedException {
        return facade.register(
                username,
                "password",
                username + "@example.com"
        );
    }

    private int createGameAndGetId(String authToken, String gameName)
            throws IOException, InterruptedException {
        facade.createGame(authToken, gameName);

        List<ServerFacade.GameInfo> games = facade.listGame(authToken);
        assertFalse(games.isEmpty());

        return games.getFirst().gameID();
    }

}
