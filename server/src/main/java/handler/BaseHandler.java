package handler;

import json.JsonUtil;
import spark.*;
import java.util.function.Function;

public class BaseHandler<Req, Res> {
    private final Function<Req, Res> serviceFunction;
    private final Class<Req> requestClass;

    /**
     * @param serviceFunction takes in a Request and returns a Response
     * @param requestClass Class token for JSON deserialization
     */
    public BaseHandler(Function<Req, Res> serviceFunction, Class<Req> requestClass) {
        this.serviceFunction = serviceFunction;
        this.requestClass = requestClass;
    }

    /**
     * This method allows Spark to accept
     * a handler and DAO method: handler:handleRequest
     */
    public Object handleRequest(Request req, Response res) {
        Req requestObj = JsonUtil.fromJson(req.body(), requestClass);
        Res resultObj = serviceFunction.apply(requestObj);
        res.type("application/json");
        return JsonUtil.toJson(resultObj);
    }
}
