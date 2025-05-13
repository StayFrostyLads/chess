package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static chess.ChessGame.TeamColor.WHITE;
import static chess.ChessPiece.PieceType.*;

public class PieceMovesCalculator {

    private final List<ChessPiece.PieceType> PROMOTION_OPTIONS =
            List.of(ROOK, KNIGHT, BISHOP, QUEEN);

    private final int[][] PAWN_MOVE_OFFSET = {
            {1, 1}, {1, -1}
    };

    private final int[][] ROOK_DIRECTION = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private final int[][] BISHOP_DIRECTION = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private final int[][] QUEEN_DIRECTION = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private final int[][] KING_DIRECTION = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private final int[][] KNIGHT_DIRECTION = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {-1, 2}, {1, -2}, {-1, -2}
    };

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            return Collections.emptyList();
        }
        return switch (piece.getPieceType()) {
            case PAWN -> calculatePawnMoves(board, position);
            case ROOK -> calculateRookMoves(board, position);
            case KNIGHT -> calculateKnightMoves(board, position);
            case BISHOP -> calculateBishopMoves(board, position);
            case QUEEN -> calculateQueenMoves(board, position);
            case KING -> calculateKingMoves(board, position);
        };
    }

    private boolean isEmpty(ChessBoard board, int row, int col) {
        // if on the board & if there is no piece on the desired end square
        return row >= 1 && row <= 8 && col >= 1 && col <= 8 &&
                board.getPiece(new ChessPosition(row, col)) == null;
    }

    private boolean isEnemy(ChessBoard board, ChessPosition position, int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        ChessPiece piece = board.getPiece(position);
        ChessPiece enemy = board.getPiece(new ChessPosition(row, col));
        return (enemy != null && piece.getTeamColor() != enemy.getTeamColor());
    }

    private Collection<ChessMove> helpSlidingPieces(ChessBoard board, ChessPosition position,
                                                    int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();

        for (var direction : directions) {
            int rowDirection = direction[0];
            int colDirection = direction[1];
            int row = position.getRow() + rowDirection;
            int col = position.getColumn() + colDirection;
            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                if (isEmpty(board, row, col)) {
                    moves.add(new ChessMove(position, new ChessPosition(row, col), null));
                } else {
                    if (isEnemy(board, position, row, col)) {
                        moves.add(new ChessMove(position, new ChessPosition(row, col), null));
                    }
                    break;
                }
                row += rowDirection;
                col += colDirection;
            }
        }
        return moves;
    }

    private Collection<ChessMove> calculateRookMoves(ChessBoard board, ChessPosition position) {
        return helpSlidingPieces(board, position, ROOK_DIRECTION);
    }

    private Collection<ChessMove> calculateBishopMoves(ChessBoard board, ChessPosition position) {
        return helpSlidingPieces(board, position, BISHOP_DIRECTION);
    }

    private Collection<ChessMove> calculateQueenMoves(ChessBoard board, ChessPosition position) {
        return helpSlidingPieces(board, position, QUEEN_DIRECTION);
    }

    private Collection<ChessMove> helpKingKnightMoves(ChessBoard board, ChessPosition position,
                                                      int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();

        for (var direction : directions) {
            int row = position.getRow() + direction[0];
            int col = position.getColumn() + direction[1];
            if (isEmpty(board, row, col)) {
                moves.add(new ChessMove(position, new ChessPosition(row, col), null));
            } else {
                if (isEnemy(board, position, row, col)) {
                    moves.add(new ChessMove(position, new ChessPosition(row, col), null));
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> calculateKnightMoves(ChessBoard board, ChessPosition position) {
        return helpKingKnightMoves(board, position, KNIGHT_DIRECTION);
    }

    private Collection<ChessMove> calculateKingMoves(ChessBoard board, ChessPosition position) {
        return helpKingKnightMoves(board, position, KING_DIRECTION);
    }

    private void helpPawnMoves(ChessBoard board, List<ChessMove> moves, ChessPosition position, int rowDirection,
                               int colDirection, ChessPiece.PieceType promotedPiece) {
        int row = position.getRow() + rowDirection;
        int col = position.getColumn() + colDirection;
        if (colDirection == 0) {
            if (isEmpty(board, row, col)) {
                moves.add(new ChessMove(position, new ChessPosition(row, col), promotedPiece));
            }
        } else {
            if (isEnemy(board, position, row, col)) {
                moves.add(new ChessMove(position, new ChessPosition(row, col), promotedPiece));
            }
        }
    }

    private void checkForPromotion(ChessBoard board, List<ChessMove> moves, ChessPosition position, int rowDirection, int colDirection) {
        for (ChessPiece.PieceType promotedPiece : PROMOTION_OPTIONS) {
            helpPawnMoves(board, moves, position, rowDirection, colDirection, promotedPiece);
        }
    }

    private Collection<ChessMove> calculatePawnMoves(ChessBoard board, ChessPosition position) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(position);

        int forward = pawn.getTeamColor() == WHITE ? 1 : -1;
        int promotionRow = pawn.getTeamColor() == WHITE ? 8 : 1;
        int oneRow = position.getRow() + forward;
        // 1 space forward
        if (oneRow >= 1 && oneRow <= 8) {
            if (oneRow == promotionRow) {
                if (isEmpty(board, oneRow, position.getColumn())) {
                    checkForPromotion(board, moves, position, forward, 0);
                }
            } else {
                helpPawnMoves(board, moves, position, forward, 0, null);
            }
        }

        // 2 space forward
        int homeRank = pawn.getTeamColor() == WHITE ? 2 : 7;
        if (position.getRow() == homeRank) {
            int midRow = position.getRow() + forward;
            int col = position.getColumn();
            int endRow = position.getRow() + 2*forward;
            if (isEmpty(board, midRow, col) && isEmpty(board, endRow, col)) {
                helpPawnMoves(board, moves, position, forward*2, 0, null);
            }
        }

        // diagonal captures
        for (var offset : PAWN_MOVE_OFFSET) {
            int rowDirection = forward * offset[0];
            int colDirection = offset[1];
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