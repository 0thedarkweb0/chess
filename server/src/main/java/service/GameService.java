package service;

import dataaccess.*;
import model.GameData;
import service.records.*;

import java.util.ArrayList;
import java.util.List;

public class GameService{
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGameResult listGames(String authToken) throws DataAccessException {
        if (authToken == null || authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("unauthorized");
        }

        List<GameListItem> result = new ArrayList<>();
        for (GameData game : gameDAO.listGames()) {
            result.add(new GameListItem(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName()
            ));
        }

        return new ListGameResult(result);
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request)
            throws BadRequestException, DataAccessException {
        if (authToken == null || authDAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("unauthorized");
        }

        if (request == null || request.gameName() == null
                || request.gameName().isBlank()) {
            throw new BadRequestException("missing game name");
        }

        int gameID = gameDAO.createGame(request.gameName());
        return new CreateGameResult(gameID);
    }

    public void joinGame(String authToken, JoinGameRequest request)
            throws BadRequestException, AlreadyTakenException, DataAccessException {
        var auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        if (request == null
                || (!"WHITE".equals(request.playerColor())
                && !"BLACK".equals(request.playerColor()))) {
            throw new BadRequestException("invalid request");
        }

        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("game does not exist");
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();

        if ("WHITE".equals(request.playerColor())) {
            if (white != null) {
                throw new AlreadyTakenException("color already taken");
            }
            white = auth.username();
        } else {
            if (black != null) {
                throw new AlreadyTakenException("color already taken");
            }
            black = auth.username();
        }

        gameDAO.updateGame(new GameData(
                game.gameID(), white, black, game.gameName(), game.game()));
    }
}