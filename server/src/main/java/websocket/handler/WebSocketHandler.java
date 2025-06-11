package websocket.handler;

import com.google.gson.Gson;
import websocket.server.GsonFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.*;
import websocket.messages.*;
import dataaccess.databaseimplementation.*;
import dataaccess.*;
import service.*;

import java.io.IOException;

/**
 * Handles all WebSocket traffic on /ws
 */
@WebSocket
public class WebSocketHandler {
    private static final Gson gson = GsonFactory.websocketBuilder().create();

    private final AuthDAO authDAO = new SQLAuthDAO();
    private final GameDAO gameDAO = new SQLGameDAO();
    private final UserDAO userDAO = new SQLUserDAO();
    private final AuthService authService = new AuthService(authDAO, gameDAO, userDAO);
    private final GameService gameService = new GameService(gameDAO, authService);

    private final WebSocketSession session = new WebSocketSession();
}
