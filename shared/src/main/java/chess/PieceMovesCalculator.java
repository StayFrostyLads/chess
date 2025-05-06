package chess;

import java.util.*;

import static chess.ChessGame.TeamColor.WHITE;

public class PieceMovesCalculator {
    private static final int[][] BISHOP_MOVES = {
            {1, 1,}, {1, -1}, {-1, 1}, {-1, -1}
    };
    private static final int[][] ROOK_MOVES = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private static final int[][] QUEEN_MOVES = {
            {1, 1,}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private static final int[][] KNIGHTS_MOVES = {
            {2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}
    };
    private static final int[][] KING_MOVES = {
            {1,1},{1,0},{1,-1},{0,1},{0,-1},{-1,1},{-1,0},{-1,-1}
    };

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            return Collections.emptyList();
        }
        return switch (piece.getPieceType()) {
            case KING -> calculateKingMoves(board, position);
            case QUEEN -> calculateQueenMoves(board, position);
            case ROOK -> calculateRookMoves(board, position);
            case BISHOP -> calculateBishopMoves(board, position);
            case KNIGHT -> calculateKnightMoves(board, position);
            case PAWN -> calculatePawnMoves(board, position);
        };
    }

    /**
     * The helper method for the moving functionality of the Queen, Bishop, and Rook
     *
     * @param board The chess board
     * @param startPosition The starting position of the piece being moved
     * @param directions An array of arrays that passes in the moves possible for a queen, bishop, or rook
     * @return moves: An array list of possible moves for the king or knight based on the current position
     */
    private Collection<ChessMove> createSlidingPieces(ChessBoard board, ChessPosition startPosition, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();
        for (var direction : directions) {
            int rowDirection = direction[0], colDirection = direction[1];
            int row = startPosition.getRow(), col = startPosition.getColumn();
            while (true) {
                row += rowDirection;
                col += colDirection;
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                }
                ChessPosition endPosition = new ChessPosition(row, col);
                ChessPiece endSquarePiece = board.getPiece(endPosition);
                if (endSquarePiece == null) {
                    moves.add(new ChessMove(startPosition, endPosition, null));
                } else {
                    if (endSquarePiece.getTeamColor() != board.getPiece(startPosition).getTeamColor()) {
                        moves.add(new ChessMove(startPosition, endPosition, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }

    /**
     * A helper method for the moving functionality of the King and Knight
     *
     * @param board The chess board
     * @param startPosition The starting position of the piece being moved
     * @param directions An array of arrays that passes in the moves possible for a king or knight
     * @return moves: An array list of possible moves for the king or knight based on the current position
     */
    private Collection<ChessMove> createSteppingPieces(ChessBoard board, ChessPosition startPosition, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);

        for (var direction : directions) {
            int row = direction[0] + startPosition.getRow();
            int col = direction[1] + startPosition.getColumn();
            if (row < 1 || row > 8 || col < 1 || col > 8) {
                continue;
            }
            ChessPosition endPosition = new ChessPosition(row, col);
            ChessPiece endSquare = board.getPiece(endPosition);
            if (endSquare == null || endSquare.getTeamColor() != piece.getTeamColor()) {
                moves.add(new ChessMove(startPosition, endPosition, null));
            }
        }
        return moves;
    }

    public Collection<ChessMove> calculateKingMoves(ChessBoard board, ChessPosition position) {
        return createSteppingPieces(board, position, KING_MOVES);
    }

    public Collection<ChessMove> calculateQueenMoves(ChessBoard board, ChessPosition position) {
        return createSlidingPieces(board, position, QUEEN_MOVES);
    }

    public Collection<ChessMove> calculateRookMoves(ChessBoard board, ChessPosition position) {
        return createSlidingPieces(board, position, ROOK_MOVES);
    }

    public Collection<ChessMove> calculateBishopMoves(ChessBoard board, ChessPosition position) {
        return createSlidingPieces(board, position, BISHOP_MOVES);
    }

    public Collection<ChessMove> calculateKnightMoves(ChessBoard board, ChessPosition position) {
        return createSteppingPieces(board, position, KNIGHTS_MOVES);
    }

    /**
     * 
     * @param board The chess board
     * @param possibleMoves A list of the possible moves available for the pawn based on the position
     * @param startPosition The starting position of the piece moving
     * @param rowDirection The direction of the row
     * @param colDirection The direction of the column
     * @param promotionPiece Whether the pawn can promote
     */
    private void helpPawnMoves(ChessBoard board, List<ChessMove> possibleMoves, ChessPosition startPosition, int rowDirection, int colDirection, ChessPiece.PieceType promotionPiece) {
        int row = startPosition.getRow() + rowDirection, col = startPosition.getColumn() + colDirection;
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return;
        }
        ChessPosition endPosition = new ChessPosition(row, col);
        ChessPiece endSquarePiece = board.getPiece(endPosition);
        ChessPiece piece = board.getPiece(startPosition);
        if (colDirection == 0) {
            if (endSquarePiece == null) {
                possibleMoves.add(new ChessMove(startPosition, endPosition, promotionPiece));
            } else {
                if (endSquarePiece != null && endSquarePiece.getTeamColor() != piece.getTeamColor()) {
                    possibleMoves.add(new ChessMove(startPosition, endPosition, promotionPiece));
                }
            }
        }
    }

    /**
     *
     * @param position The position of the pawn to be offset
     * @param rowDirection The direction of the row
     * @param colDirection The direction of the column
     * @return The now offset position of the pawn
     */
    private ChessPosition offset(ChessPosition position, int rowDirection, int colDirection) {
        return new ChessPosition(position.getRow() + rowDirection, position.getColumn() + colDirection);
    }

    /**
     *
     * @param board The chess board
     * @param position The position of the pawn
     * @return An array list of moves that the pawn can make
     */
    public Collection<ChessMove> calculatePawnMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(position);
        if (pawn == null) {
            return moves;
        }
        int direction = pawn.getTeamColor() == WHITE ? +1 : -1;
        helpPawnMoves(board, moves, position, direction, 0, null);

        int homeRank = pawn.getTeamColor() == WHITE ? 2 : 7;
        if (position.getRow() == homeRank) {
            if (board.getPiece(offset(position, direction, 0)) == null) {
                helpPawnMoves(board, moves, position, direction*2, 0, null);
            }
        }

        helpPawnMoves(board, moves, position, direction, 1, null);
        helpPawnMoves(board, moves, position, direction, -1, null);

        return moves;
    }
}
