package client;

import com.google.gson.Gson;
import model.AuthData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ServerFacade {
    private final String serverUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
    public record GameInfo(
            int gameID,
            String whiteUsername,
            String blackUsername,
            String gameName
    ) {}
    private record JoinGameRequestBody(String playerColor, int gameID) {}
    private record ListGamesResponse(List<GameInfo> games) {}
    private record CreateGameRequestBody(String gameName) {}
    private record CreateGameResponse(int gameID) {}
    private record RegisterRequestBody(
            String username,
            String password,
            String email
    ) {}
    private record LoginRequestBody(String username, String password) {}

    public AuthData login(String username, String password)
            throws IOException, InterruptedException {
        var body = gson.toJson(new LoginRequestBody(username, password));

        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), AuthData.class);
        } else if (response.statusCode() == 401) {
            throw new IllegalStateException("Username or password is incorrect.");
        } else if (response.statusCode() == 400) {
            throw new IllegalStateException("Enter your username and password.");
        } else {
            throw new IllegalStateException("The server could not complete the login.");
        }
    }

    public AuthData register(String username, String password, String email)
            throws IOException, InterruptedException {
        var body = gson.toJson(
                new RegisterRequestBody(username, password, email)
        );

        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), AuthData.class);
        } else if (response.statusCode() == 403) {
            throw new IllegalStateException("That username is already taken.");
        } else if (response.statusCode() == 400) {
            throw new IllegalStateException("Enter a username, password, and email.");
        } else {
            throw new IllegalStateException("The server could not complete registration.");
        }
    }

    public void logout(String authToken)
            throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/session"))
                .header("authorization", authToken)
                .DELETE()
                .build();

        var response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 401) {
            throw new IllegalStateException("Your session is no longer valid. Please log in again.");
        } else if (response.statusCode() != 200) {
            throw new IllegalStateException("The server could not complete logout.");
        }
    }

    public void createGame(String authToken, String gameName)
    throws IOException, InterruptedException {
        var body = gson.toJson(new CreateGameRequestBody(gameName));
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .header("Content-Type", "application/json")
                .header("authorization", authToken)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 200) {
            CreateGameResponse result =
                    gson.fromJson(response.body(), CreateGameResponse.class);
        } else if (response.statusCode() == 400) {
            throw new IllegalStateException("Enter a valid game name.");
        } else if (response.statusCode() == 401) {
            throw new IllegalStateException("Your session is invalid. Please login again");
        } else {
            throw new IllegalStateException("The server could not create the game");
        }
    }

        public List<GameInfo> listGame(String authToken)
        throws IOException, InterruptedException{
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/game"))
                    .header("authorization", authToken)
                    .GET()
                    .build();

            var response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                ListGamesResponse result =
                        gson.fromJson(response.body(), ListGamesResponse.class);
                return result.games();
            } else if (response.statusCode() == 401) {
                throw new IllegalStateException("Your session is invalid. Please log in again.");
            } else {
                throw new IllegalStateException("The server could not list the games.");
            }
        }
    public void joinGame(String authToken, int gameID, String playerColor)
            throws IOException, InterruptedException {
        var body = gson.toJson(new JoinGameRequestBody(playerColor, gameID));

        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .header("Content-Type", "application/json")
                .header("authorization", authToken)
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 200) {
            return;
        } else if (response.statusCode() == 400) {
            throw new IllegalStateException("Choose a valid game and color.");
        } else if (response.statusCode() == 401) {
            throw new IllegalStateException("Your session is invalid. Please log in again.");
        } else if (response.statusCode() == 403) {
            throw new IllegalStateException("That color is already taken.");
        } else {
            throw new IllegalStateException("The server could not join the game.");
        }
    }

    }
