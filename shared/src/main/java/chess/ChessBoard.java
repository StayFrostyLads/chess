package chess;
import java.util.Arrays;
import java.util.Objects;

import static chess.ChessGame.TeamColor.*;
import static chess.ChessPiece.PieceType.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private final ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow()-1;
        int col = position.getColumn()-1;
        squares[row][col] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow()-1;
        int col = position.getColumn()-1;
        return squares[row][col];
    }

    /**
     *
     * @param piece The desired special chess piece to place on the back row
     * @param color The desired color of the chess piece
     * @return piece, color
     */

    private ChessPiece makePiece(ChessPiece.PieceType piece, ChessGame.TeamColor color) {
        return switch (piece) {
            case PAWN -> new Pawn(color);
            case ROOK -> new Rook(color);
            case KNIGHT -> new Knight(color);
            case BISHOP -> new Bishop(color);
            case QUEEN -> new Queen(color);
            case KING -> new King(color);
        };
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = null;
            }
        }

        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(2, col), makePiece(PAWN, WHITE));
            addPiece(new ChessPosition(7, col), makePiece(PAWN, BLACK));
        }
        ChessPiece.PieceType [] backRank = { ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};
        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(1, col), makePiece(backRank[col-1], WHITE));
            addPiece(new ChessPosition(8, col), makePiece(backRank[col-1], BLACK));
        }
    }

}
