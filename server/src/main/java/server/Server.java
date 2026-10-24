package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import service.ClearService;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var gameDAO = new MemoryGameDAO();

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


    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
