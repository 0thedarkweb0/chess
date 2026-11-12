package client;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import static ui.EscapeSequences.*;

import chess.*;
import model.AuthData;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println("Welcome to Chris's 240 chess. Type help to get started");
        Scanner scanner = new Scanner(System.in);
        ServerFacade facade = new ServerFacade(8080);
        AuthData currentUser = null;
        boolean running = true;
        List<ServerFacade.GameInfo> games = List.of();
        while (running) {
            System.out.print("> ");
            String command = scanner.nextLine().trim().toLowerCase();
            if(currentUser==null) {
                switch (command) {
                    case "help":
                        System.out.println("Available commands: login, register, help, quit");
                        break;
                    case "quit":
                        running = false;
                        break;

                    case "login":
                        System.out.print("Username: ");
                        String username = scanner.nextLine();

                        System.out.print("Password: ");
                        String password = scanner.nextLine();

                        try {
                            currentUser = facade.login(username, password);
                            System.out.println("Login successful. Welcome, " + currentUser.username() + ".");
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                        } catch (IOException e) {
                            System.out.println("Could not connect to the server.");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Login was interrupted.");
                        }break;
                    case "register":
                        System.out.print("Username: ");
                        String newUsername = scanner.nextLine();
                        System.out.print("Password: ");
                        String newPassword = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        try{
                            currentUser = facade.register(newUsername, newPassword, email);
                            System.out.println("Registration successful. Welcome, " + currentUser.username() + ".");
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                        } catch (IOException e) {
                            System.out.println("Could not connect to the server.");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Registration was interrupted.");
                        }break;
                    default:
                        System.out.println("Unknown command. Type help to see available commands.");}
            }else{
                System.out.println("Commands: help, logout, create game, list games, play game, observe, quit.");
                switch (command) {
                    case "help":
                        System.out.println("Commands: help, logout, create game, list games, play game, observe, quit");
                        break;
                    case "logout":
                        try {
                            facade.logout(currentUser.authToken());
                            currentUser = null;
                            System.out.println("Logged out.");
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                        } catch (IOException e) {
                            System.out.println("Could not connect to the server.");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Logout was interrupted.");
                        }
                        break;
                    case "create game":
                        System.out.print("Game Name: ");
                        String gameName = scanner.nextLine();
                        try{
                            facade.createGame(currentUser.authToken(), gameName);
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                        } catch (IOException e) {
                            System.out.println("Could not connect to the server.");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Create games was interrupted.");
                        }break;
                    case "list games":
                        System.out.print("Games: ");
                        try{
                           games = facade.listGame(currentUser.authToken());
                           if(games.isEmpty()){
                               System.out.println("There are no games.");
                           }else{
                               for (int i = 0; i < games.size(); i++) {
                                   ServerFacade.GameInfo game = games.get(i);
                                   String white = game.whiteUsername() == null
                                           ? "open" : game.whiteUsername();
                                   String black = game.blackUsername() == null
                                           ? "open" : game.blackUsername();
                                   System.out.printf(
                                           "%d. %s (White: %s, Black: %s)%n",
                                           i + 1, game.gameName(), white, black
                                   );
                               }
                           }
                        } catch (IllegalStateException e){
                            System.out.println(e.getMessage());
                        } catch (IOException e){
                            System.out.println("Could not connect to the server");
                        } catch (InterruptedException e){
                            Thread.currentThread().interrupt();
                            System.out.println("List games was interrupted");
                        }break;

                    case "play game": {
                        if (games.isEmpty()) {
                            System.out.println("List games first. There are no games to select.");
                            break;
                        }

                        System.out.print("Game number: ");
                        int number;
                        try {
                            number = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Enter a number from the game list.");
                            break;
                        }

                        if (number < 1 || number > games.size()) {
                            System.out.println("That game number is not in the list.");
                            break;
                        }

                        System.out.print("Color (WHITE or BLACK): ");
                        String color = scanner.nextLine().trim().toUpperCase();
                        if (!color.equals("WHITE") && !color.equals("BLACK")) {
                            System.out.println("Choose WHITE or BLACK.");
                            break;
                        }
                        ServerFacade.GameInfo selectedGame = games.get(number - 1);
                        try {
                            facade.joinGame(
                                    currentUser.authToken(),
                                    selectedGame.gameID(),
                                    color
                            );
                            System.out.println("Joined " + selectedGame.gameName() + " as " + color + ".");
                            drawBoard(new ChessGame().getBoard(), color.equals("BLACK"));
                        }catch (IllegalStateException e){
                            System.out.println(e.getMessage());
                        }catch (IOException e){
                            System.out.println("Could not connect to the server.");
                        }catch (InterruptedException e){
                            Thread.currentThread().interrupt();
                            System.out.println("Joining the game was interrupted.");
                        }break;}

                    case "observe": {
                        if(games.isEmpty()){
                            System.out.println("List games first so you can choose a game.");
                            break;}

                        System.out.print("Game number: ");
                        int number;
                        try{
                            number = Integer.parseInt(scanner.nextLine().trim());
                        }catch (NumberFormatException e) {
                            System.out.println("Enter a number from the game list.");
                            break;}
                        if(number < 1 || number > games.size()){
                            System.out.println("That game number is not in the list.");
                            break;}
                        ServerFacade.GameInfo selectedGame = games.get(number - 1);
                        System.out.println("Observing " + selectedGame.gameName() + ".");
                        drawBoard(new ChessGame().getBoard(), false);
                        break;}
                    case "quit":
                        running = false;
                        break;
                    default:
                        System.out.println("Unknown command. Type help to see available commands.");
                }}}
        scanner.close();}
    private static void drawBoard(ChessBoard board, boolean blackPerspective) {
        int row = blackPerspective ? 1 : 8;
        int rowEnd = blackPerspective ? 8 : 1;
        int rowStep = blackPerspective ? 1 : -1;
        int col = blackPerspective ? 8 : 1;
        int colEnd = blackPerspective ? 1 : 8;
        int colStep = blackPerspective ? -1 : 1;
        System.out.print("   ");
        for (int c = col; ; c += colStep) {
            System.out.printf(" %c ", (char) ('a' + c - 1));
            if (c == colEnd) {break;}
        }
        System.out.println();

        for (int r = row; ; r += rowStep) {
            System.out.print(r + " ");

            for (int c = col; ; c += colStep) {
                boolean lightSquare = (r + c) % 2 == 1;
                String background = lightSquare
                        ? SET_BG_COLOR_LIGHT_GREY
                        : SET_BG_COLOR_DARK_GREEN;

                ChessPiece piece = board.getPiece(new ChessPosition(r, c));
                System.out.print(background + pieceText(piece) + RESET_BG_COLOR);

                if (c == colEnd){break;}
            }

            System.out.println(" " + r);
            if (r == rowEnd) {break;}
        }

        System.out.print("   ");
        for (int c = col; ; c += colStep) {
            System.out.printf(" %c ", (char) ('a' + c - 1));
            if (c == colEnd) {break;}
        }
        System.out.println();
    }

    private static String pieceText(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }

        String symbol = switch(piece.getPieceType()){
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? WHITE_QUEEN : BLACK_QUEEN;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? WHITE_ROOK : BLACK_ROOK;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? WHITE_KNIGHT : BLACK_KNIGHT;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE
                    ? WHITE_PAWN : BLACK_PAWN;
        };

        String textColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                ?  SET_TEXT_COLOR_RED
                : SET_TEXT_COLOR_BLUE;

        return textColor + symbol + RESET_TEXT_COLOR;
    }

}

