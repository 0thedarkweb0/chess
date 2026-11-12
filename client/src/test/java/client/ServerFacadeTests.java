package client;

import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

}
