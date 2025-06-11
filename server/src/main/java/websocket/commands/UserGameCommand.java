package websocket.commands;

import chess.ChessMove;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;

    private final String authToken;

    private final Integer gameID;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand)) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }

    public static class ConnectCommand extends UserGameCommand {
        public ConnectCommand(String authToken, Integer gameID) {
            super(CommandType.CONNECT, authToken, gameID);
        }
    }

    public static class MakeMoveCommand extends UserGameCommand {
        private final ChessMove move;

        public MakeMoveCommand(String authToken, Integer gameID, ChessMove move) {
            super(CommandType.MAKE_MOVE, authToken, gameID);
            this.move = move;
        }

        public ChessMove getMove() {
            return move;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MakeMoveCommand)) return false;
            if (!super.equals(o)) return false;
            MakeMoveCommand that = (MakeMoveCommand) o;
            return Objects.equals(move, that.move);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), move);
        }
    }

    public static class LeaveCommand extends UserGameCommand {
        public LeaveCommand(String authToken, Integer gameID) {
            super(CommandType.LEAVE, authToken, gameID);
        }
    }

    public static class ResignCommand extends UserGameCommand {
        public ResignCommand(String authToken, Integer gameID) {
            super(CommandType.RESIGN, authToken, gameID);
        }
    }
}
