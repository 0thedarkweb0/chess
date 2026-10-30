package server;
import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.MySqlDatabase;
import dataaccess.sql.SqlAuthDAO;
import dataaccess.sql.SqlGameDAO;
import dataaccess.sql.SqlUserDAO;
import io.javalin.*;
import service.*;
import service.records.*;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();

    public Server() {
        try {
            MySqlDatabase.initialize();
        } catch (DataAccessException e) {
            throw new IllegalStateException("Could not initialize the database", e);}
        var userDAO = new SqlUserDAO();
        var authDAO = new SqlAuthDAO();
        var gameDAO = new SqlGameDAO();
        var gameService = new GameService(authDAO, gameDAO);
        var userService = new UserService(userDAO, authDAO);

        var clearService = new ClearService(userDAO,authDAO,gameDAO);
        javalin = Javalin.create(config ->
                config.staticFiles.add("web"));
        javalin.exception(DataAccessException.class,
                (e, ctx) -> sendError(ctx, 500, "Error: database operation failed"));
        javalin.exception(BadRequestException.class,
                (e, ctx) -> sendError(ctx, 400, "Error: bad request"));
        javalin.exception(UnauthorizedException.class,
                (e, ctx) -> sendError(ctx, 401, "Error: unauthorized"));
        javalin.exception(AlreadyTakenException.class,
                (e, ctx) -> sendError(ctx, 403, "Error: already taken"));

        javalin.delete("/db",ctx-> {
            clearService.clear(); ctx.status(200).result("{}");});
        javalin.post("/user", ctx -> {
            ctx.contentType("application/json");
                RegisterRequest request =
                        gson.fromJson(ctx.body(), RegisterRequest.class);
                RegisterResult result = userService.register(request);
                ctx.status(200).result(gson.toJson(result));
            });
        javalin.post("/session", ctx -> {
            ctx.contentType("application/json");
                LoginRequest request =
                        gson.fromJson(ctx.body(), LoginRequest.class);
                LoginResult result = userService.login(request);
                ctx.status(200).result(gson.toJson(result));
            });
        javalin.delete("/session", ctx -> {
            ctx.contentType("application/json");
                userService.logout(ctx.header("authorization"));
                ctx.status(200).result("{}");
            });
        javalin.get("/game", ctx -> {
            ctx.contentType("application/json");
                ListGameResult result =
                        gameService.listGames(ctx.header("authorization"));
                ctx.status(200).result(gson.toJson(result));
            });
        javalin.post("/game", ctx -> {
            ctx.contentType("application/json");
                CreateGameRequest request =
                        gson.fromJson(ctx.body(), CreateGameRequest.class);
                CreateGameResult result =
                        gameService.createGame(ctx.header("authorization"), request);
                ctx.status(200).result(gson.toJson(result));
            });
        javalin.put("/game", ctx -> {
            ctx.contentType("application/json");
                JoinGameRequest request =
                        gson.fromJson(ctx.body(), JoinGameRequest.class);
                gameService.joinGame(ctx.header("authorization"), request);
                ctx.status(200).result("{}");
           });
    }
    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();}
    public void stop() {javalin.stop();}

    private void sendError(io.javalin.http.Context ctx, int status, String message) {
        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(Map.of("message", message)));
    }

}