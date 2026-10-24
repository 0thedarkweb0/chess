package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UnauthorizedException;
import model.GameData;

import java.util.ArrayList;
import java.util.List;

public class GameService{
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListGameResult listGames(String authToken) {
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
}