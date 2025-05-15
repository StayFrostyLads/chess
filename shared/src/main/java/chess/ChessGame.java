package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor team;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.team = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return team;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.team = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK;

        public TeamColor other() {
            return this == WHITE ? BLACK : WHITE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessGame)) {
            return false;
        }
        ChessGame that = (ChessGame) o;
        if (this.board == null || this.team == null || that.board == null || that.team == null) {
            return false;
        }
        return board.equals(that.board) && team == that.team;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, team);
    }

    /**
     * Deep clone helper
     *
     * @param original The original board to be copied
     * @param copy The deep copy of the board
     */
    private void cloneBoard(ChessBoard original, ChessBoard copy) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                copy.addPiece(position, original.getPiece(position));
            }
        }
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null;
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        TeamColor color = piece.getTeamColor();
        List<ChessMove> legalMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {
            ChessBoard copy = new ChessBoard();
            cloneBoard(board, copy);
            copy.addPiece(move.getEndPosition(), board.getPiece(startPosition));
            copy.addPiece(startPosition, null);
            ChessGame trial = new ChessGame();
            trial.setBoard(copy);
            if (!trial.isInCheck(color)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("There is no piece located at: " + move.getStartPosition());
        }
        if (piece.getTeamColor() != team) {
            throw new InvalidMoveException("It is not currently " + team + "'s turn");
        }
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Your move: " + move + " is not legal!");
        }
        board.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), board.makePiece(move.getPromotionPiece(), piece.getTeamColor()));
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }

        team = team.other();
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING
                        && piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null;
    }

    private boolean isSquareAttacked(ChessPosition position, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition startPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(startPosition);
                if (piece == null) {
                    continue;
                }
                if (piece.getTeamColor() != teamColor) {
                    continue;
                }
                for (ChessMove move : piece.pieceMoves(board, startPosition)) {
                    if (move.getEndPosition().equals(position)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        return isSquareAttacked(kingPosition, teamColor.other());
    }

    private boolean checkBoard(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (getTeamTurn() != teamColor || !isInCheck(teamColor)) {
            return false;
        }
        return checkBoard(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (getTeamTurn() != teamColor || isInCheck(teamColor)) {
            return false;
        }
        return checkBoard(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
