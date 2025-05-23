package handler;

import json.JsonUtil;
import service.*;
import spark.*;
import dataaccess.*;
import dataaccess.implementation.*;


public class ClearHandler {
    private final ClearService service;

    public ClearHandler() {
        AuthDAO authDAO = new InMemoryAuthDAO();
        GameDAO gameDAO = new InMemoryGameDAO();
        UserDAO userDAO = new InMemoryUserDAO();
        this.service = new ClearService(authDAO, gameDAO, userDAO);
    }
    /**
     *
     */
    public Object handleRequest(Request req, Response res) {
        ClearRequest request = JsonUtil.fromJson(req.body(), ClearRequest.class);
        ClearResult result = service.clear(request);
        res.type("application/json");
        return JsonUtil.toJson(result);
    }
}


