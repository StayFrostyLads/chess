package websocket.handler;

import chess.ChessGame;
import com.google.gson.Gson;
import handler.ExceptionHandler;
import websocket.messages.ServerMessage;
import websocket.server.GsonFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.*;
import websocket.commands.UserGameCommand.*;
import dataaccess.databaseimplementation.*;
import dataaccess.*;
import service.*;
import service.GameService.*;

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

    private final WebSocketSessions sessions = new WebSocketSessions();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        return;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws DataAccessException {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            if (command instanceof ConnectCommand connect) {
                handleConnect(session, connect);
            } else if (command instanceof MakeMoveCommand move) {
                handleMakeMove(session, move);
            } else if (command instanceof LeaveCommand leave) {
                handleLeave(session, leave);
            } else if (command instanceof ResignCommand resign) {
                handleResign(session, resign);
            }
        } catch (Exception e) {
            String exceptionMessage = ExceptionHandler.mapToErrorMessage(e);
            send(session, new ServerMessage.Error(exceptionMessage));
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        sessions.removeSession(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable ex) {
        String errorMessage = ExceptionHandler.mapToErrorMessage(ex);

        if (session.isOpen()) {
            try {
                session.getRemote().sendString(gson.toJson(new ServerMessage.Error(errorMessage)));
            } catch (IOException e) {
                // this should VERY RARELY happen in my testing environment
                System.out.println("There was an IO error: " + e.getMessage());
            }
        }
        sessions.removeSession(session);
    }

    private void handleConnect(Session session, ConnectCommand command) throws DataAccessException {
        var authToken = authDAO.getAuth(command.getAuthToken());
        if (authToken.isEmpty()) {
            send(session, new ServerMessage.Error("Invalid auth token"));
            return;
        }

        var result = gameService.observeGame(command.getAuthToken(), command.getGameID());
        if (!result.success()) {
            send(session, new ServerMessage.Error(result.message()));
            return;
        }

        ChessGame game = result.game();
        ChessGame.TeamColor role = result.playerRole();

        String side;
        if (role == ChessGame.TeamColor.WHITE) {
            side = "white";
        } else if (role == ChessGame.TeamColor.BLACK) {
            side = "black";
        } else {
            side = "observer";
        }

        sessions.addSessionToGame(command.getGameID(), session);
        send(session, new ServerMessage.LoadGame(game));

        String user = authToken.get().username();

        var notification = new ServerMessage.Notification(user + " joined as " + side);
        sessions.getSessionsForGame(command.getGameID()).stream()
                .filter(s -> !s.equals(session)).forEach(s -> send(s, notification));
    }

    private void handleMakeMove(Session session, MakeMoveCommand command) throws DataAccessException {
        MakeMoveResult moveResult = gameService.makeMove(command.getAuthToken(), command.getGameID(),
                                                        command.getMove());

        if (!moveResult.success()) {
            send(session, new ServerMessage.Error(moveResult.message()));
            return;
        }

        ServerMessage.LoadGame load = new ServerMessage.LoadGame(moveResult.game());
        broadcast(command.getGameID(), load);

        ServerMessage.Notification moveNotification = new ServerMessage.Notification(moveResult.notification());
        sessions.getSessionsForGame(command.getGameID()).stream()
                                    .filter(s -> ! s.equals(session))
                                    .forEach(s -> send(s, moveNotification));

        if (moveResult.isCheckmate()) {
            String winner = moveResult.game().getTeamTurn().other().name();
            broadcast(command.getGameID(),
                    new ServerMessage.Notification("Checkmate! " + winner + " has won the game!"));
        } else if (moveResult.isCheck()) {
            String inCheck = moveResult.game().getTeamTurn().name();
            broadcast(command.getGameID(), new ServerMessage.Notification("Player " + inCheck + " is in check!"));
        }

    }

    private void handleLeave(Session session, LeaveCommand command) throws DataAccessException {
        try {
            gameService.leaveGame(command.getAuthToken(), command.getGameID());
        } catch (Exception e) {
            send(session, new ServerMessage.Error("Error leaving the game: " + e.getMessage()));
            return;
        }
        sessions.removeSession(session);

        String username = authDAO.getAuth(command.getAuthToken()).orElseThrow().username();
        ServerMessage.Notification notification = new ServerMessage.Notification(username + " has left the game");
        sessions.getSessionsForGame(command.getGameID()).stream()
                .filter(s -> !s.equals(session))
                .forEach(s -> send(s, notification));
    }

    private void handleResign(Session session, ResignCommand command) throws DataAccessException {
        try {
            gameService.resignGame(command.getAuthToken(), command.getGameID());
        } catch (Exception e) {
            send(session, new ServerMessage.Error("Error resigning: " + e.getMessage()));
            return;
        }

        String username = authDAO.getAuth(command.getAuthToken()).orElseThrow().username();
        ServerMessage.Notification notification = new ServerMessage.Notification(username + " resigned");
        broadcast(command.getGameID(), notification);
    }

    private void send(Session otherSession, ServerMessage serverMessage) {
        try {
            otherSession.getRemote().sendString(gson.toJson(serverMessage));
        } catch (IOException e) {
            // this should VERY RARELY happen in my testing environment
            System.out.println("There was an IO error: " + e.getMessage());
            sessions.removeSession(otherSession);
        }
    }

    private void broadcast(int gameID, ServerMessage serverMessage) {
        sessions.getSessionsForGame(gameID).forEach(session -> send(session, serverMessage));
    }
}
