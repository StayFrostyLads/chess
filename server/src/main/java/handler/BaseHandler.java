package handler;

import com.google.gson.Gson;
import spark.*;
import java.util.function.Function;

// Q is request, A is response
public class BaseHandler<Q, A> {

    private final Gson gson = new Gson();

    private final Function<Q, A> serviceFunction;
    private final Class<Q> requestClass;

    /**
     * @param serviceFunction takes in a Request and returns a Response
     * @param requestClass Class token for JSON deserialization
     */
    public BaseHandler(Function<Q, A> serviceFunction, Class<Q> requestClass) {
        this.serviceFunction = serviceFunction;
        this.requestClass = requestClass;
    }

    /**
     * This method allows Spark to accept
     * a handler and DAO method: handler:handleRequest
     */
    public Object handleRequest(Request req, Response res) {
        Q requestObj = gson.fromJson(req.body(), requestClass);
        A resultObj = serviceFunction.apply(requestObj);
        res.type("application/json");
        return gson.toJson(resultObj);
    }
}
