package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.implementation.InMemoryAuthDAO;
import dataaccess.implementation.InMemoryGameDAO;
import dataaccess.implementation.InMemoryUserDAO;
import handler.*;
import service.*;
import spark.*;

import java.util.Map;

public class Server {

    private final Gson gson = new Gson();

    AuthDAO authDAO = new InMemoryAuthDAO();
    GameDAO gameDAO = new InMemoryGameDAO();
    UserDAO userDAO = new InMemoryUserDAO();

    ClearService clearService = new ClearService(authDAO, gameDAO, userDAO);
    BaseHandler<ClearRequest, ClearResult> clearHandler = new BaseHandler<>(
            clearService::clear,
            ClearRequest.class
    );

    RegisterService registerService = new RegisterService(userDAO);
    BaseHandler<RegisterRequest, RegisterResult> registerHandler = new BaseHandler<>(
            registerService::register,
            RegisterRequest.class
    );

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("/web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", registerHandler::handleRequest);

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
