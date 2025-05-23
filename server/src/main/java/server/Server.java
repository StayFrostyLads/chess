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
    BaseHandler<ClearRequest, ClearResult> clearHandler = new BaseHandler<>(
            clearService::clear,
            ClearRequest.class
    );

    RegisterService registerService = new RegisterService(userDAO, authDAO);
    BaseHandler<RegisterRequest, RegisterResult> registerHandler = new BaseHandler<>(
            registerService::register,
            RegisterRequest.class
    );

    LoginService loginService = new LoginService(userDAO, authDAO);
    BaseHandler<LoginRequest, LoginResult> loginHandler = new BaseHandler<>(
            loginService::login,
            LoginRequest.class
    );

    LogoutService logoutService = new LogoutService(authDAO);
    ListGamesService listGamesService = new ListGamesService(gameDAO);
    BaseHandler<ListGamesRequest, ListGamesResult> listGamesHandler = new BaseHandler<>(
            listGamesService::listGames,
            ListGamesRequest.class
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
            String token = request.headers("authorization");
            AuthData auth = authService.validate(token);
            request.attribute("auth", auth);
        });

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", registerHandler::handleRequest);
        Spark.post("/session", loginHandler::handleRequest);

        Spark.delete("/session", (request, response) -> {
            String token = request.headers("authorization");
            if (token == null || token.isBlank()) {
                throw new AuthenticationException("Missing authToken " + token);
            }

            LogoutResult result = logoutService.logout(new LogoutRequest(token));
            response.type("application/json");
            return JsonUtil.toJson(result);
        });
        Spark.get("/game", listGamesHandler::handleRequest);
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
