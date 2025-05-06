package chess;

import java.util.*;

public class PieceMovesCalculator {
    private final int[][] BISHOP_DIRECTION = {
            {1, 1,}, {1, -1}, {-1, 1}, {-1, -1}
    };
    private final int[][] ROOK_DIRECTION = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private final int[][] QUEEN_DIRECTION = {
            {1, 1,}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private final int[][] KNIGHTS_MOVES = {
            {2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}
    };
    private final int[][] KING_MOVES = {
            {1,1},{1,0},{1,-1},{0,1},{0,-1},{-1,1},{-1,0},{-1,-1}
    };

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position){
        ChessPiece piece = board.getPiece(position);
        if (piece == null) return Collections.emptyList();
        return switch (piece.getPieceType()) {
            case KING -> calculateKingMoves(board, position);
            case QUEEN -> calculateQueenMoves(board, position);
            case ROOK -> calculateRookMoves(board, position);
            case BISHOP -> calculateBishopMoves(board, position);
            case KNIGHT -> calculateKnightMoves(board, position);
            case PAWN -> calculatePawnMoves(board, position);
        };
    }

    private Collection<ChessMove> createSlidingPieces(ChessBoard board, ChessPosition startPosition, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();
        for (var direction : directions) {
            int rowDirection = direction[0];
            int colDirection = direction[1];
            int row = startPosition.getRow(), col = startPosition.getColumn();
            while (true) {
                row += rowDirection;
                col += colDirection;
                if (row < 1 || row > 8 || col < 1 || col > 8) break;
                ChessPosition endingPosition = new ChessPosition(row, col);
                ChessPiece endSquarePiece = board.getPiece(endingPosition);
                if (endSquarePiece == null) {
                    moves.add(new ChessMove(startPosition, endingPosition, null));
                } else {
                    if (endSquarePiece.getTeamColor() != board.getPiece(startPosition).getTeamColor()) {
                        moves.add(new ChessMove(startPosition, endingPosition, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> createStepMoves(ChessBoard board, ChessPosition startPosition, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();
        return moves;
    }

    public Collection<ChessMove> calculateKingMoves(ChessBoard board, ChessPosition position) {
        return new ArrayList<>();
    }

    public Collection<ChessMove> calculateQueenMoves(ChessBoard board, ChessPosition position) {
        return createSlidingPieces(board, position, QUEEN_DIRECTION);
    }

    public Collection<ChessMove> calculateRookMoves(ChessBoard board, ChessPosition position) {
        return createSlidingPieces(board, position, ROOK_DIRECTION);
    }

    public Collection<ChessMove> calculateBishopMoves(ChessBoard board, ChessPosition position) {
        return createSlidingPieces(board, position, BISHOP_DIRECTION);
    }

    public Collection<ChessMove> calculateKnightMoves(ChessBoard board, ChessPosition position) {
        return new ArrayList<>();
    }

    public Collection<ChessMove> calculatePawnMoves(ChessBoard board, ChessPosition position) {
        return new ArrayList<>();
    }
}
