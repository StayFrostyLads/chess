package server;

import com.google.gson.Gson;
import dataaccess.*;
import dataaccess.implementation.*;
import handler.*;
import json.JsonUtil;
import model.AuthData;
import service.*;
import spark.*;

import java.util.Map;

public class Server {

    private final Gson gson = new Gson();

    AuthDAO authDAO = new InMemoryAuthDAO();
    GameDAO gameDAO = new InMemoryGameDAO();
    UserDAO userDAO = new InMemoryUserDAO();

    AuthService authService = new AuthService(authDAO);

    ClearService clearService = new ClearService(authDAO, gameDAO, userDAO);
    BaseHandler<ClearService.Request, ClearService.Result> clearHandler = new BaseHandler<>(
            request -> clearService.clear(),
            ClearService.Request.class
    );

    RegisterService registerService = new RegisterService(userDAO, authDAO);
    BaseHandler<RegisterService.Request, RegisterService.Result> registerHandler = new BaseHandler<>(
            registerService::register,
            RegisterService.Request.class
    );

    LoginService loginService = new LoginService(userDAO, authDAO);
    BaseHandler<LoginService.Request, LoginService.Result> loginHandler = new BaseHandler<>(
            loginService::login,
            LoginService.Request.class
    );

    LogoutService logoutService = new LogoutService(authDAO);

    ListGamesService listGamesService = new ListGamesService(authDAO, gameDAO);
    BaseHandler<ListGamesService.Request, ListGamesService.Result> listGamesHandler = new BaseHandler<>(
            listGamesService::listGames,
            ListGamesService.Request.class
    );

    CreateGameService createGameService = new CreateGameService(authDAO, gameDAO);
    BaseHandler<CreateGameService.Request, CreateGameService.Result> createGameHandler = new BaseHandler<>(
            createGameService::createGame,
            CreateGameService.Request.class
    );


    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("/web");

        Spark.before((request, response) -> {
            String path = request.pathInfo();
            String method = request.requestMethod();

            if (method.equals("POST") && (path.equals("/user") || (path.equals("/session")))) {
                return;
            }
            if (method.equals("DELETE") && (path.equals("/db"))) {
                return;
            }
            String token = request.headers("Authorization");
            AuthData auth = authService.validate(token);
            request.attribute("auth", auth);
        });

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", registerHandler::handleRequest);
        Spark.post("/session", loginHandler::handleRequest);

        Spark.delete("/session", (request, response) -> {
            String token = request.headers("Authorization");
            if (token == null || token.isBlank()) {
                throw new AuthenticationException("Missing authToken " + token);
            }

            LogoutService.Result result = logoutService.logout(new LogoutService.Request(token));
            response.type("application/json");
            return JsonUtil.toJson(result);
        });
        Spark.get("/game", (request, response) -> {
            String token = request.headers("Authorization");
            if (token == null || token.isBlank()) {
                throw new AuthenticationException("Missing authToken " + token);
            }

            ListGamesService.Result result = listGamesService.listGames(new ListGamesService.Request(token));
            response.type("application/json");
            return JsonUtil.toJson(result);
        });
        Spark.post("/game", (request, response) -> {
            String token = request.headers("Authorization");
            if (token == null || token.isBlank()) {
                throw new AuthenticationException("Missing authToken " + token);
            }

            CreateGameService.Request bodyRequest = JsonUtil.fromJson(request.body(), CreateGameService.Request.class);
            if (bodyRequest == null || bodyRequest.gameName() == null || bodyRequest.gameName().isBlank()) {
                throw new BadRequestException("Missing or empty gameName");
            }

            CreateGameService.Request fullRequest = new CreateGameService.Request(token, bodyRequest.gameName());

            CreateGameService.Result result = createGameService.createGame(fullRequest);
            response.type("application/json");
            return JsonUtil.toJson(result);
        });
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
        if (e instanceof AlreadyTakenException) {
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
