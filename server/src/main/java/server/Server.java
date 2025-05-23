package server;

import com.google.gson.Gson;
import handler.ClearHandler;
import spark.*;

import java.util.Map;

public class Server {

    private final Gson gson = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("/web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", (req, res) ->
                (new ClearHandler()).handleRequest(req, res));
        Spark.get("/error", this::throwError);

        Spark.exception(Exception.class, this::errorHandler);
        Spark.notFound((req, res) -> {
            res.type("application/json");
            res.status(404);
            return gson.toJson(Map.of(
                    "success", false,
                    "message", String.format("[%s] %s not found",
                            req.requestMethod(), req.pathInfo())
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

    private Object throwError(Request req, Response res) {
        throw new RuntimeException("Test runtime exception");
    }

    public void errorHandler(Exception e, Request req, Response res) {
        res.status(500);
        res.type("application/json");

        String body = gson.toJson(Map.of(
                "success", false,
                "message", String.format("Error %s", e.getMessage())
        ));
        res.body(body);
    }
}
