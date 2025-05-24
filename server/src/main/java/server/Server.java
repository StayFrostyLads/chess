package server;

import com.google.gson.Gson;
import dataaccess.*;
import dataaccess.implementation.*;
import handler.*;
import json.JsonUtil;
import service.*;
import spark.*;

import java.util.Map;

public class Server {

    private final Gson gson = new Gson();

    private final AuthDAO authDAO = new InMemoryAuthDAO();
    private final GameDAO gameDAO = new InMemoryGameDAO();
    private final UserDAO userDAO = new InMemoryUserDAO();

    private final AuthService authService = new AuthService(authDAO, gameDAO, userDAO);
    private final UserService userService = new UserService(userDAO, authDAO, authService);
    private final GameService gameService = new GameService(gameDAO, authService);

    private final BaseHandler<UserService.RegisterRequest, UserService.AuthResult> registerHandler = new BaseHandler<>(
            request -> userService.register(request.username(), request.password(),
                    request.email()), UserService.RegisterRequest.class);

    private final BaseHandler<UserService.LoginRequest, UserService.AuthResult> loginHandler = new BaseHandler<>(
            request -> userService.login(request.username(), request.password()),
                        UserService.LoginRequest.class
    );

    private final BaseHandler<Void, AuthService.ClearResult> clearHandler =
            new BaseHandler<>(request -> authService.clearDatabase(), Void.class);

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("/web");

        // Register your endpoints and handle exceptions here.

        // Register
        Spark.post("/user", registerHandler::handleRequest);

        // Login
        Spark.post("/session", loginHandler::handleRequest);

        // Logout
        Spark.delete("/session", (request, response) -> {
            String token = request.headers("Authorization");
            if (token == null || token.isBlank()) {
                throw new AuthenticationException("Missing authToken " + token);
            }

            UserService.LogoutResult result = userService.logout(token);
            response.type("application/json");
            return JsonUtil.toJson(result);
        });

        // List Games
        Spark.get("/game", (request, response) -> {
            String token = request.headers("Authorization");
            if (token == null || token.isBlank()) {
                throw new AuthenticationException("Missing authToken " + token);
            }

            GameService.ListGamesResult result = gameService.listGames(token);
            response.type("application/json");
            return JsonUtil.toJson(result);
        });

        // Create Game
        Spark.post("/game", (request, response) -> {
            String token = request.headers("Authorization");
            GameService.CreateGameRequest body = JsonUtil.fromJson(request.body(), GameService.CreateGameRequest.class);
            if (body == null || body.gameName() == null || body.gameName().isBlank()) {
                throw new BadRequestException("Missing or empty gameName");
            }

            GameService.CreateGameResult result = gameService.createGame(token, body.gameName());
            response.type("application/json");
            return JsonUtil.toJson(result);
        });

        // Join Game
        Spark.put("/game", (request, response) -> {
            String token = request.headers("Authorization");

            GameService.JoinGameRequest body = JsonUtil.fromJson(request.body(), GameService.JoinGameRequest.class);
            if (body == null) {
                throw new BadRequestException("Missing request body");
            }

            GameService.JoinGameResult result = gameService.joinGame(token, body.gameID(), body.playerColor());
            response.type("application/json");
            return JsonUtil.toJson(result);
        });

        // Clear Databases
        Spark.delete("/db", clearHandler::handleRequest);
        Spark.get("/error", this::throwError);

        Spark.exception(Exception.class, this::errorHandler);
        Spark.notFound((request, response) -> {
            response.type("application/json");
            response.status(404);
            return gson.toJson(Map.of(
                    "success", false,
                    "message", String.format("[%s] %s not found",
                            request.requestMethod(), request.pathInfo())
            ));
        });

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object throwError(Request request, Response response) {
        throw new RuntimeException("Test runtime exception");
    }

    public void errorHandler(Exception e, Request request, Response response) {
        int statusCode;
        if (e instanceof AlreadyTakenException || e instanceof ForbiddenException) {
            statusCode = 403;
        } else if (e instanceof AuthenticationException) {
            statusCode = 401;
        } else if (e instanceof ServerException) {
            statusCode = 500;
        } else if (e instanceof BadRequestException) {
            statusCode = 400;
        } else {
            statusCode = 500;
        }

        String body = gson.toJson(Map.of(
                "success", false,
                "message", String.format("Error %s", e.getMessage())
        ));

        response.status(statusCode);
        response.type("application/json");
        response.body(body);
    }
}
