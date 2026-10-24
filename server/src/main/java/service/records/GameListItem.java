package service.records;

public record GameListItem(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName
) {}
