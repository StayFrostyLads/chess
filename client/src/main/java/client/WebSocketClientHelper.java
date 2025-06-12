package client;

import java.net.URI;
import java.net.http.*;
import java.util.concurrent.CompletionStage;

import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.server.GsonFactory;
import websocket.commands.UserGameCommand.*;
import websocket.messages.ServerMessage;
import chess.ChessMove;

public class WebSocketClientHelper {
    public interface Listener {
        void onLoadGame(ServerMessage.LoadGame message);
        void onNotification(ServerMessage.Notification message);
        void onError(ServerMessage.Error message);
    }

    private final Gson gson = GsonFactory.websocketBuilder().create();
    private final String authToken;
    private final int gameID;
    private final WebSocket webSocket;
    private final Listener listener;

    public WebSocketClientHelper(URI webSocketURI, String authToken, int gameID, Listener listener) {
        this.listener = listener;
        this.authToken = authToken;
        this.gameID = gameID;
        webSocket = HttpClient.newHttpClient().newWebSocketBuilder()
                    .buildAsync(webSocketURI, new WebSocket.Listener() {
                        @Override
                        public void onOpen(WebSocket webSocket) {
                            ConnectCommand command = new ConnectCommand(authToken, gameID);
                            webSocket.sendText(gson.toJson(command), true);
                            webSocket.request(1);
                        }
                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            ServerMessage message = gson.fromJson(data.toString(), ServerMessage.class);
                            if (message instanceof ServerMessage.LoadGame loadGame) {
                                listener.onLoadGame(loadGame);
                            } else if (message instanceof ServerMessage.Notification notification) {
                                listener.onNotification(notification);
                            } else if (message instanceof ServerMessage.Error errorMessage) {
                                listener.onError(errorMessage);
                            }
                            webSocket.request(1);
                            return null;
                        }
                        @Override
                        public void onError(WebSocket webSocket, Throwable ex) {
                            listener.onError(new ServerMessage.Error("WebSocket connection error: " + ex.getMessage()));
                        }
        }).join();
    }

    private ChessMove parseMoveNotation(String notation) {
        notation = notation.trim();
        if (notation.length() != 4 && notation.length() != 5) {
            throw new IllegalArgumentException("Expected format: 'move <e2e4>', or 'move <e7e8q>' for promotion");
        }
        String fromAlgebraic = notation.substring(0, 2);
        String toAlgebraic = notation.substring(2, 4);

        ChessPosition from = ChessPosition.fromAlgebraic(fromAlgebraic);
        ChessPosition to = ChessPosition.fromAlgebraic(toAlgebraic);

        ChessPiece.PieceType promotionPiece = getPromotionPiece(notation);
        return new ChessMove(from, to, promotionPiece);
    }

    private static ChessPiece.PieceType getPromotionPiece(String notation) {
        ChessPiece.PieceType promotionPiece = null;
        if (notation.length() == 5) {
            char p = notation.charAt(4);
            promotionPiece = switch (p) {
                case 'q' -> ChessPiece.PieceType.QUEEN;
                case 'r' -> ChessPiece.PieceType.ROOK;
                case 'b' -> ChessPiece.PieceType.BISHOP;
                case 'n' -> ChessPiece.PieceType.KNIGHT;
                default -> throw new
                        IllegalArgumentException("Unknown promotion piece: " + p + ", expected 'q', 'r', 'b' or 'n'");
            };
        }
        return promotionPiece;
    }

    public void sendMove(String moveNotation) {
        ChessMove move = parseMoveNotation(moveNotation);
        sendText(new MakeMoveCommand(authToken, gameID, move));
    }

    public void sendResign() {
        sendText(new ResignCommand(authToken, gameID));
    }

    public void sendLeave() {
        sendText(new LeaveCommand(authToken, gameID));
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Thanks for playing!");
    }

    public void sendText(UserGameCommand command) {
        String payload = gson.toJson(command);
        webSocket.sendText(payload, true);
    }

}
