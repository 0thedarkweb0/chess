package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.sql.MySqlDatabase;
import dataaccess.sql.SqlAuthDAO;
import dataaccess.sql.SqlGameDAO;
import dataaccess.sql.SqlUserDAO;
import io.javalin.*;
import service.*;
import service.records.*;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        try {
            MySqlDatabase.initialize();
        } catch (DataAccessException e) {
            throw new IllegalStateException("Could not initialize the database", e);
        }
        var userDAO = new SqlUserDAO();
        var authDAO = new SqlAuthDAO();
        var gameDAO = new SqlGameDAO();
        var gameService = new GameService(authDAO, gameDAO);
        var userService = new UserService(userDAO, authDAO);
        var gson = new Gson();
        var clearService = new ClearService(userDAO,authDAO,gameDAO);
        javalin = Javalin.create(config ->
                config.staticFiles.add("web"));

        javalin.exception(DataAccessException.class, (e, ctx) -> {
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(Map.of(
                    "message", "Error: database operation failed"
            )));
        });

        javalin.delete("/db",ctx-> {
            clearService.clear(); ctx.status(200).result("{}");});
        javalin.post("/user", ctx -> {
            ctx.contentType("application/json");
            try {
                RegisterRequest request =
                        gson.fromJson(ctx.body(), RegisterRequest.class);
                RegisterResult result = userService.register(request);
                ctx.status(200).result(gson.toJson(result));
            } catch (BadRequestException e) {
                ctx.status(400).result(
                        gson.toJson(Map.of("message", "Error: bad request")));
            } catch (AlreadyTakenException e) {
                ctx.status(403).result(
                        gson.toJson(Map.of("message", "Error: already taken")));
            }});
        javalin.post("/session", ctx -> {
            ctx.contentType("application/json");
            try {
                LoginRequest request =
                        gson.fromJson(ctx.body(), LoginRequest.class);
                LoginResult result = userService.login(request);
                ctx.status(200).result(gson.toJson(result));
            } catch (BadRequestException e) {
                ctx.status(400).result(
                        gson.toJson(Map.of("message", "Error: bad request")));
            } catch (UnauthorizedException e) {
                ctx.status(401).result(
                        gson.toJson(Map.of("message", "Error: unauthorized")));
            }});
        javalin.delete("/session", ctx -> {
            ctx.contentType("application/json");
            try {
                userService.logout(ctx.header("authorization"));
                ctx.status(200).result("{}");
            } catch (UnauthorizedException e) {
                ctx.status(401).result(
                        gson.toJson(Map.of("message", "Error: unauthorized")));
            }});
        javalin.get("/game", ctx -> {
            ctx.contentType("application/json");
            try {
                ListGameResult result =
                        gameService.listGames(ctx.header("authorization"));
                ctx.status(200).result(gson.toJson(result));
            } catch (UnauthorizedException e) {
                ctx.status(401).result(
                        gson.toJson(Map.of("message", "Error: unauthorized")));
            }});
        javalin.post("/game", ctx -> {
            ctx.contentType("application/json");
            try {
                CreateGameRequest request =
                        gson.fromJson(ctx.body(), CreateGameRequest.class);
                CreateGameResult result =
                        gameService.createGame(ctx.header("authorization"), request);
                ctx.status(200).result(gson.toJson(result));
            } catch (BadRequestException e) {
                ctx.status(400).result(
                        gson.toJson(Map.of("message", "Error: bad request")));
            } catch (UnauthorizedException e) {
                ctx.status(401).result(
                        gson.toJson(Map.of("message", "Error: unauthorized")));
            }});
        javalin.put("/game", ctx -> {
            ctx.contentType("application/json");
            try {
                JoinGameRequest request =
                        gson.fromJson(ctx.body(), JoinGameRequest.class);
                gameService.joinGame(ctx.header("authorization"), request);
                ctx.status(200).result("{}");
            } catch (BadRequestException e) {
                ctx.status(400).result(
                        gson.toJson(Map.of("message", "Error: bad request")));
            } catch (UnauthorizedException e) {
                ctx.status(401).result(
                        gson.toJson(Map.of("message", "Error: unauthorized")));
            } catch (AlreadyTakenException e) {
                ctx.status(403).result(
                        gson.toJson(Map.of("message", "Error: already taken")));
            } catch (DataAccessException e) {
                ctx.status(500).result(
                        gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }});


    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }
    public void stop() {
        javalin.stop();
    }
}
