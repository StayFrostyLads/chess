package client;

import java.util.*;
import java.util.stream.Collectors;

import chess.ChessBoard;
import chess.ChessGame;
import exception.ResponseException;
import server.ServerFacade;
import ui.ChessBoardPrinter;

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

    public String getUsername() {
        return this.username;
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

        if (state == State.PRELOGIN) {
            return handlePrelogin(command, params);
        } else {
            return handlePostlogin(command, params);
        }
    }

    private String handlePrelogin(String command, String[] params) {
        try {
            return switch (command) {
                case "login" -> login(params);
                case "register" -> register(params);
                default -> "Unknown command, Type \"help\" for a list of commands.";
            };
        } catch (RuntimeException ex) {
            return ex.getMessage();
        }
    }

    private String login(String[] params) {
        if (params.length != 2) {
            return "Expected format: login <USERNAME> <PASSWORD>";
        }
        String username = params[0];
        String password = params[1];
        ServerFacade.AuthResult result;
        try {
            result = server.login(new ServerFacade.LoginRequest(username, password));
        } catch (ServerFacade.AuthenticationException e) {
            String message = e.getMessage();
            if (message.toLowerCase().contains("invalid username")) {
                return "Login failed: no user exists with the name \"" + username + "\".";
            } else if (message.toLowerCase().contains("invalid password")) {
                return "Login failed: incorrect password for user \"" + username + "\".";
            } else {
                return "Login failed: " + message;
            }
        } catch (ServerFacade.ServerException e) {
            return "Login failed (server error): " + e.getMessage();
        }

        this.authToken = result.authToken();
        this.username = username;
        server.setAuthToken(this.authToken);
        this.state = State.POSTLOGIN;
        return "Login successful. Welcome to Jack's Chess Client, " + username + "!";

    }

    private String register(String[] params) {
        if (params.length != 3) {
            return "Expected format: register <USERNAME> <PASSWORD> <EMAIL>";
        }
        String username = params[0];
        String password = params[1];
        String email = params[2];
        ServerFacade.AuthResult result;
        try {
            result = server.register(new ServerFacade.RegisterRequest(username, password, email));
        } catch (ServerFacade.AlreadyTakenException e) {
            return "Register failed, the username: " + username + " is already taken!";
        } catch (ServerFacade.ServerException e) {
            return "Register failed (server error): " + e.getMessage();
        }

        this.authToken = result.authToken();
        this.username = username;
        server.setAuthToken(this.authToken);
        this.state = State.POSTLOGIN;
        return "Registration successful. You are now logged in as " + username + ".";

    }

    private String handlePostlogin(String command, String[] params) {
        try {
            return switch (command) {
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                default -> "Unknown command, Type \"help\" for a list of commands.";
            };
        } catch (RuntimeException ex) {
            return ex.getMessage();
        }
    }

    private String logout() {
        server.logout();
        this.authToken = null;
        this.username = null;
        this.listedGames = List.of();
        this.state = State.PRELOGIN;
        return "Logged out. Returning to the Prelogin UI! Play again soon!";
    }

    private String createGame(String[] params) {
        if (params.length < 1) {
            return "Expected format: create <NAME>";
        }
        String gameName = String.join(" ", params);
        var request = new ServerFacade.CreateGameRequest(gameName);
        var result = server.createGame(request);
        var game = result.game();
        int newGameID = game.gameID();

        var listResult = server.listGames();
        var gameEntry = listResult.games();

        return String.format("Created game \"%s\".", game.gameName());
    }

    private String listGames() {
        var result = server.listGames();
        var gameEntry = result.games();
        if (gameEntry == null || gameEntry.length == 0) {
            listedGames = List.of();
            return "No games currently being played.\n";
        }

        listedGames = Arrays.stream(gameEntry).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listedGames.size(); i++) {
            var game = listedGames.get(i);
            String whitePlayer = (game.whiteUsername() == null ? "-" : game.whiteUsername());
            String blackPlayer = (game.blackUsername() == null ? "-" : game.blackUsername());
            sb.append(String.format("%d. \"%s\" [%s vs %s]\n", i+1, game.gameName(), whitePlayer, blackPlayer));
        }
        return sb.toString();
    }

    private String joinGame(String[] params) {
        if (params.length != 2) {
            return "Expected format: join <ID> <WHITE|BLACK>";
        }
        int index;
        try {
            index = Integer.parseInt(params[0]) - 1;
        } catch (NumberFormatException nf) {
            return "Invalid input for game number. Please type a valid integer shown by \"list\".";
        }
        if (index < 0 || index >= listedGames.size()) {
            return "Invalid game number. Please select a valid game number given by \"list\".";
        }

        var selectedGame = listedGames.get(index);
        int gameID = selectedGame.gameID();
        boolean playerIsWhite = selectedGame.whiteUsername() != null && selectedGame.whiteUsername().equals(username);
        boolean playerIsBlack = selectedGame.blackUsername() != null && selectedGame.blackUsername().equals(username);

        String color = params[1].toUpperCase();
        if (playerIsWhite || playerIsBlack) {
            color = playerIsWhite ? "WHITE" : "BLACK";
        } else {
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                return "Invalid color. Please use either WHITE or BLACK.";
            }
        }

        if (!playerIsWhite && !playerIsBlack) {
            var request = new ServerFacade.JoinGameRequest(gameID, color);
            var result = server.joinGame(request);
            if (!result.success()) {
                return "Failed to join the specified game: server refused. " +
                        "Check if the player slot is already taken or if you are already in the game!";
            }
        }

        ChessBoard board = new ChessBoard();
        board.resetBoard();
        // will update to real game state later

        boolean whitePerspective = color.equals("WHITE");
        ChessBoardPrinter.printBoard(board, whitePerspective);

        if (playerIsWhite || playerIsBlack) {
            return String.format(
                    "You have rejoined the game \"%s\" (ID=%d) as %s.  (Board printed above)",
                    selectedGame.gameName(), index + 1, color
            );
        } else {
            return String.format("You successfully joined \"%s\" (ID=%d) as %s.  (Board printed above)",
                    selectedGame.gameName(), index + 1, color);
        }
    }

    private String observeGame(String[] params) {
        if (params.length != 1) {
            return "Expected format: observe <ID>";
        }
        int index;
        try {
            index = Integer.parseInt(params[0]) - 1;
        } catch (NumberFormatException nf) {
            return "Invalid input for game number. Please type a valid integer shown by \"list\".";
        }
        if (index < 0 || index >= listedGames.size()) {
            return "Invalid game number. Please select a valid game number shown by \"list\".";
        }

        int realGameID = listedGames.get(index).gameID();

        ServerFacade.ObserveGameResult observeResult;
        try {
            observeResult = server.observeGame(realGameID);
        } catch (RuntimeException e) {
            return e.getMessage();
        }

        if (!observeResult.success()) {
            return "Observe failed: " + observeResult.message();
        }

        ChessGame liveGame = observeResult.game();

        ChessBoard board = liveGame.getBoard();
        ChessBoardPrinter.printBoard(board, true);
        return "Board was printed above, you are now spectating game: " + listedGames.get(index).gameName() + ".";
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
                    create <NAME>               - Create a game with the specified game name
                    list                        - List all current existing games
                    join <ID> [WHITE][BLACK]    - Join an existing game as white/black
                    observe <ID>                - Watch an ongoing game
                    logout                      - Logout of this chess client
                    help                        - Show this help page
                    quit                        - Exit this program
                    """;
        }
    }

    public boolean isPostLogin() {
        return this.state == State.POSTLOGIN;
    }

}