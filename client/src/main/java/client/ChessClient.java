package client;

import java.util.*;
import exception.ResponseException;
import server.ServerFacade;

public class ChessClient {
    private enum State {
        PRELOGIN,
        POSTLOGIN
    }

    private State state = State.PRELOGIN;
    private String username = null;
    private String authToken = null;
    private final ServerFacade server;
    private List<ServerFacade.GameEntry> listedGames = List.of();

    public ChessClient(String url) {
        this.server = new ServerFacade(url);
    }

    public String eval(String rawInput) {
        String input = rawInput.trim();
        if (input.isEmpty()) {
            return "";
        }

        String[] tokens = input.split("\\s+");
        String command = tokens[0].toLowerCase();
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

        if (command.equals("quit")) {
            return "quit";
        }
        if (command.equals("help")) {
            return help();
        }
        return handlePrelogin(command, params);

//        if (state == State.PRELOGIN) {
//            return handlePrelogin(command, params);
//        } //else {
//            return handlePostlogin(command, params);
//        }
    }

    private String handlePrelogin(String command, String[] params) {
        try {
            switch (command) {
                case "login":
                    return login(params);
                case "register":
                    return register(params);
                default:
                    return "Unknown command, Type \"help\" for a list of commands.";
            }
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String login(String[] params) throws ResponseException {
        if (params.length == 2) {
            String username = params[0];
            String password = params[1];
            var result = server.login(new ServerFacade.LoginRequest(username, password));
            this.authToken = result.authToken();
            this.username = username;
            server.setAuthToken(this.authToken);
            this.state = State.POSTLOGIN;
            return "Login successful. Welcome to Jack's Chess Client, " + username + "!";
        }
        return "Expected format: login <USERNAME> <PASSWORD>";
    }

    private String register(String[] params) throws ResponseException {
        if (params.length == 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            var result = server.register(new ServerFacade.RegisterRequest(username, password, email));
            this.authToken = result.authToken();
            this.username = username;
            server.setAuthToken(this.authToken);
            this.state = State.POSTLOGIN;
            return "Registration successful. You are now logged in as " + username + ".";
        }
        return "Expected format: register <USERNAME> <PASSWORD> <EMAIL>";
    }

    public String help() {
        if (state == State.PRELOGIN) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL>  - Create an account and log in
                    login <USERNAME> <PASSWORD>             - Log in to an already existing account
                    help                                    - Show this help page
                    quit                                    - Exit this program
                    """;
        } else {
            return """
                    POSTLOGIN STUFF HERE
                    """;
        }
    }

}