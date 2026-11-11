package client;
import java.util.Scanner;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

import chess.*;

public class ClientMain {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    public static void main(String[] args) {
        System.out.println("Welcome to Chris's 240 chess. Type help to get started");
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {
            System.out.print("> ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help":
                    System.out.println("Available commands: help, quit");
                    break;
                case "quit":
                    running = false;
                    break;
                default:
                    System.out.println("Unknown command. Type help to see available commands.");
            }
        }

        scanner.close();

    }
}
