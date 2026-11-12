package client;
import java.io.IOException;
import java.util.Scanner;


import chess.*;
import model.AuthData;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println("Welcome to Chris's 240 chess. Type help to get started");
        Scanner scanner = new Scanner(System.in);
        ServerFacade facade = new ServerFacade(8080);
        AuthData currentUser = null;
        boolean running = true;

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
                        }
                        break;

                    case "register":
                        System.out.print("Username: ");
                        String newUsername = scanner.nextLine();

                        System.out.print("Password: ");
                        String newPassword = scanner.nextLine();

                        System.out.print("Email: ");
                        String email = scanner.nextLine();

                        try {
                            currentUser = facade.register(newUsername, newPassword, email);
                            System.out.println("Registration successful. Welcome, " + currentUser.username() + ".");
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                        } catch (IOException e) {
                            System.out.println("Could not connect to the server.");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Registration was interrupted.");
                        }
                        break;

                    default:
                        System.out.println("Unknown command. Type help to see available commands.");
                }
            }else{
                System.out.println("Commands: help, logout, create, list, play, observe, quit");

                switch (command) {
                    case "help":
                        System.out.println("Commands: help, logout, create, list, play, observe, quit");
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

                    case "create":
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
                            System.out.println("Logout was interrupted.");
                        }

                    case "quit":
                        running = false;
                        break;

                    default:
                        System.out.println("That command isn't available yet.");
                }
            }

        }

        scanner.close();

    }
}
