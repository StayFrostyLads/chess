package chess;

import java.util.Collection;
import java.util.Objects;

import static chess.ChessGame.TeamColor.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    /**
     * The constructor for determining what color the piece is and of what type it is.
     * @param pieceColor The color of the chess piece (options are white or black)
     * @param type The type of chess piece
     */
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessPiece)) {
            return false;
        }
        ChessPiece that = (ChessPiece)o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return switch (type) {
            case PAWN -> pieceColor == WHITE ? "P" : "p";
            case ROOK -> pieceColor == WHITE ? "R" : "r";
            case QUEEN -> pieceColor == WHITE ? "Q" : "q";
            case KING -> pieceColor == WHITE ? "K" : "k";
            case KNIGHT -> pieceColor == WHITE ? "N" : "n";
            case BISHOP -> pieceColor == WHITE ? "B" : "b";
        };
    }

    public static class Pawn extends ChessPiece {
        public Pawn(ChessGame.TeamColor color) {
            super(color, PieceType.PAWN);
        }
    }

    public static class Bishop extends ChessPiece {
        public Bishop(ChessGame.TeamColor color) {
            super(color, PieceType.BISHOP);
        }
    }

    public static class Knight extends ChessPiece {
        public Knight(ChessGame.TeamColor color) {
            super(color, PieceType.KNIGHT);
        }
    }

    public static class Queen extends ChessPiece {
        public Queen(ChessGame.TeamColor color) {
            super(color, PieceType.QUEEN);
        }
    }

    public static class King extends ChessPiece {
        public King(ChessGame.TeamColor color) {
            super(color, PieceType.KING);
        }
    }

    public static class Rook extends ChessPiece {
        public Rook(ChessGame.TeamColor color) {
            super(color, PieceType.ROOK);
        }
    }


    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return new PieceMovesCalculator().pieceMoves(board, myPosition);
    }
}
