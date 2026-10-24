package service;

public record GameListItem(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName
) {}
