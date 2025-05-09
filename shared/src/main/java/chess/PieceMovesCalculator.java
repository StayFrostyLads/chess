package chess;

import java.util.*;

import static chess.ChessGame.TeamColor.WHITE;
import static chess.ChessPiece.PieceType.*;

public class PieceMovesCalculator {
    private static final int[][] BISHOP_DIRECTION = {
            {1, 1,}, {1, -1}, {-1, 1}, {-1, -1}
    };
    private static final int[][] ROOK_DIRECTION = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private static final int[][] QUEEN_DIRECTION = {
            {1, 1,}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private static final int[][] KNIGHTS_DIRECTION = {
            {2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}
    };
    private static final int[][] KING_DIRECTION = {
            {1,1},{1,0},{1,-1},{0,1},{0,-1},{-1,1},{-1,0},{-1,-1}
    };
    private static final int[][] PAWN_CAPTURE_OFFSET = {
            {1,1}, {1,-1}
    };
    private static final List<ChessPiece.PieceType> PROMOTION_OPTIONS =
            List.of(QUEEN, ROOK, BISHOP, KNIGHT);

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
     * A helper method to determine whether the destination square is empty.
     * @param board The chess board
     * @param row The targeted row of the board
     * @param col The targeted column of the board
     * @return True if the square defined by (row, col) is empty, false if it is occupied
     */
    private boolean isEmpty(ChessBoard board, int row, int col) {
        return row >= 1 && row <= 8
                && col >= 1 && col <= 8
                && board.getPiece(new ChessPosition(row, col)) == null;
    }

    /**
     * A helper method to determine whether the destination square is occupied by an enemy.
     *
     * @return True if the square defined by (row, col) is occupied by an enemy
     */
    private boolean isEnemy(ChessBoard board, ChessPosition piecePosition, int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        ChessPiece enemy = board.getPiece(new ChessPosition(row, col));
        ChessPiece piece = board.getPiece(piecePosition);
        return (enemy != null && enemy.getTeamColor() != piece.getTeamColor());
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
            int rowDirection = direction[0];
            int colDirection = direction[1];
            int row = startPosition.getRow() + rowDirection;
            int col = startPosition.getColumn() + colDirection;


            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                if (isEmpty(board, row, col)) {
                    moves.add(new ChessMove(startPosition, new ChessPosition(row, col), null));
                } else {
                    if (isEnemy(board, startPosition, row, col)) {
                        moves.add(new ChessMove(startPosition, new ChessPosition(row, col), null));
                    }
                    break;
                }
                row += direction[0];
                col += direction[1];
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

        for (var direction : directions) {
            int rowDirection = direction[0];
            int colDirection = direction[1];
            int row = startPosition.getRow() + rowDirection;
            int col = startPosition.getColumn() + colDirection;

            if (isEmpty(board, row, col) || isEnemy(board, startPosition, row, col)) {
                moves.add(new ChessMove(startPosition, new ChessPosition(row, col), null));
            }
        }
        return moves;
    }

    public Collection<ChessMove> calculateKingMoves(ChessBoard board, ChessPosition position) {
        return createSteppingPieces(board, position, KING_DIRECTION);
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
        return createSteppingPieces(board, position, KNIGHTS_DIRECTION);
    }

    /**
     * 
     * @param board The chess board
     * @param possibleMoves A list of the possible moves available for the pawn based on the position
     * @param startPosition The starting position of the piece moving
     * @param rowDirection The direction of the row (+1 or -1 for forward steps)
     * @param colDirection The direction of the column (+1 or -1 for diagonal steps)
     * @param promotionPiece The piece to which the pawn can promote (null by default)
     */
    private void helpPawnMoves(ChessBoard board, List<ChessMove> possibleMoves, ChessPosition startPosition,
                               int rowDirection, int colDirection, ChessPiece.PieceType promotionPiece) {
        int row = startPosition.getRow() + rowDirection;
        int col = startPosition.getColumn() + colDirection;

        if (colDirection == 0) {
            if (isEmpty(board, row, col)) {
                possibleMoves.add(new ChessMove(startPosition, new ChessPosition(row, col), promotionPiece));
            }
        } else {
                if (isEnemy(board, startPosition, row, col)) {
                    possibleMoves.add(new ChessMove(startPosition, new ChessPosition(row, col), promotionPiece));
                }
            }
    }

    /**
     *
     * @param board The chess board
     * @param possibleMoves The list of possible moves
     * @param startPosition The starting position of the piece
     * @param rowDirection The direction of the row (+1 or -1 for forward steps)
     * @param colDirection The direction of the column (+1 or -1 for diagonal steps)
     */
    private void checkForPromotion(ChessBoard board, List<ChessMove> possibleMoves,
                                   ChessPosition startPosition, int rowDirection, int colDirection) {
        for (ChessPiece.PieceType promotedPiece : PROMOTION_OPTIONS) {
            helpPawnMoves(board, possibleMoves, startPosition, rowDirection, colDirection, promotedPiece);
        }
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
        int forward = pawn.getTeamColor() == WHITE ? 1 : -1;
        int promotionRow = pawn.getTeamColor() == WHITE ? 8 : 1;
        int oneRow = position.getRow() + forward;

        if (oneRow >= 1 && oneRow <= 8) {
            if (oneRow == promotionRow) {
                checkForPromotion(board, moves, position, forward, 0);
            } else {
                helpPawnMoves(board, moves, position, forward, 0, null);
            }
        }

        int homeRank = pawn.getTeamColor() == WHITE ? 2 : 7;
        if (position.getRow() == homeRank) {
            int midRow = position.getRow() + forward;
            int midCol = position.getColumn();

            if (isEmpty(board, midRow, midCol)) {
                helpPawnMoves(board, moves, position, forward*2, 0, null);
            }
        }

        for (var offset : PAWN_CAPTURE_OFFSET) {
            int rowDirection = forward * offset[0], colDirection = offset[1];
            int endRow = position.getRow() + rowDirection;

            if (endRow == promotionRow) {
                checkForPromotion(board, moves, position, rowDirection, colDirection);
            } else {
                helpPawnMoves(board, moves, position, rowDirection, colDirection, null);
            }
        }

        return moves;
    }
}
