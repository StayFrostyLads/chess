package websocket.messages;

import chess.ChessGame;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public abstract class ServerMessage {
    private final ServerMessageType serverMessageType;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    protected ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }

    public static class LoadGame extends ServerMessage {
        private final ChessGame game;

        public LoadGame(ChessGame game) {
            super(ServerMessageType.LOAD_GAME);
            this.game = game;
        }

        public ChessGame getGame() {
            return game;
        }
    }

    public static class Error extends ServerMessage {
        private final String errorMessage;

        public Error(String message) {
            super(ServerMessageType.ERROR);
            this.errorMessage = message;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static class Notification extends ServerMessage {
        private final String notification;

        public Notification(String message) {
            super(ServerMessageType.NOTIFICATION);
            this.notification = message;
        }

        public String getNotification() {
            return notification;
        }
    }
}
