package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import service.*;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var gameDAO = new MemoryGameDAO();
        var gameService = new GameService(authDAO, gameDAO);

        var userService = new UserService(userDAO, authDAO);
        var gson = new Gson();

        var clearService = new ClearService(userDAO,authDAO,gameDAO);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db",ctx-> {clearService.clear(); ctx.status(200).result("{}");});

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
            }
        });

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
            }
        });

        javalin.delete("/session", ctx -> {
            ctx.contentType("application/json");

            try {
                userService.logout(ctx.header("authorization"));
                ctx.status(200).result("{}");
            } catch (UnauthorizedException e) {
                ctx.status(401).result(
                        gson.toJson(Map.of("message", "Error: unauthorized")));
            }
        });

        javalin.get("/game", ctx -> {
            ctx.contentType("application/json");

            try {
                ListGameResult result =
                        gameService.listGames(ctx.header("authorization"));
                ctx.status(200).result(gson.toJson(result));
            } catch (UnauthorizedException e) {
                ctx.status(401).result(
                        gson.toJson(Map.of("message", "Error: unauthorized")));
            }
        });

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
            }
        });


    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
