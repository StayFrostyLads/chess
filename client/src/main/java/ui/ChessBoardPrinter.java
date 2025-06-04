package ui;

import chess.*;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

import static ui.EscapeSequences.*;

public class ChessBoardPrinter {

    public static void printBoard(ChessBoard board, boolean whitePerspective) {
        if (whitePerspective) {
            printWhiteSide(board);
        } else {
            printBlackSide(board);
        }
    }

    /**
     * A helper for printing the board from white's perspective
     */
    public static void printWhiteSide(ChessBoard board) {
        System.out.print(SET_BG_COLOR_BLUE);
        System.out.print("  ");
        for (char file = 'a'; file <= 'h'; file++) {
            System.out.print(" " + file + " ");
        }

        System.out.print("  ");
        System.out.print(RESET_BG_COLOR);
        System.out.println();

        for (int rank = 8; rank >= 1; rank--) {
            System.out.print(SET_BG_COLOR_BLUE + rank + " " + RESET_BG_COLOR);

            for (int fileIndex = 1; fileIndex <= 8; fileIndex++) {
                printBoardHelper(board, rank, fileIndex);
            }

            System.out.print(SET_BG_COLOR_BLUE + " " + rank + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.print(SET_BG_COLOR_BLUE);
        System.out.print("  ");
        for (char file = 'a'; file <= 'h'; file++) {
            System.out.print(" " + file + " ");
        }
        System.out.print("  ");
        System.out.print(RESET_BG_COLOR);
        System.out.println();

        System.out.print(RESET_TEXT_COLOR);
        System.out.print(RESET_BG_COLOR);
    }

    /**
     * A helper for printing the board from black's perspective
     */
    public static void printBlackSide(ChessBoard board) {
        System.out.print(SET_BG_COLOR_BLUE);
        System.out.print("  ");
        for (char file = 'h'; file >= 'a'; file--) {
            System.out.print(" " + file + " ");
        }

        System.out.print("  ");
        System.out.print(RESET_BG_COLOR);
        System.out.println();

        for (int rank = 1; rank <= 8; rank++) {
            System.out.print(SET_BG_COLOR_BLUE + rank + " " + RESET_BG_COLOR);

            for (int fileIndex = 8; fileIndex >= 1; fileIndex--) {
                printBoardHelper(board, rank, fileIndex);
            }

            System.out.print(SET_BG_COLOR_BLUE + " " + rank + RESET_BG_COLOR);
            System.out.println();
        }

        System.out.print(SET_BG_COLOR_BLUE);
        System.out.print("  ");
        for (char file = 'h'; file >= 'a'; file--) {
            System.out.print(" " + file + " ");
        }
        System.out.print("  ");
        System.out.print(RESET_BG_COLOR);
        System.out.println();

        System.out.print(RESET_TEXT_COLOR);
        System.out.print(RESET_BG_COLOR);
    }

    private static void printBoardHelper(ChessBoard board, int rank, int fileIndex) {
        boolean isLightSquare = ((rank + fileIndex) % 2 == 0);
        String backgroundColor = isLightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;

        System.out.print(backgroundColor);

        ChessPosition position = new ChessPosition(rank, fileIndex);
        ChessPiece piece = board.getPiece(position);
        String pieceSymbol = unicodeForChessPiece(piece);

        System.out.print(pieceSymbol);
        System.out.print(RESET_BG_COLOR);
    }

    /**
     * A helper to return the correct unicode character for each chess piece
     */
    private static String unicodeForChessPiece(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }

        TeamColor color = piece.getTeamColor();
        PieceType type = piece.getPieceType();

        if (color == TeamColor.WHITE) {
            switch (type) {
                case KING: return WHITE_KING;
                case QUEEN: return WHITE_QUEEN;
                case BISHOP: return WHITE_BISHOP;
                case KNIGHT: return WHITE_KNIGHT;
                case ROOK: return WHITE_ROOK;
                case PAWN: return WHITE_PAWN;
            }
        } else {
            switch (type) {
                case KING: return BLACK_KING;
                case QUEEN: return BLACK_QUEEN;
                case BISHOP: return BLACK_BISHOP;
                case KNIGHT: return BLACK_KNIGHT;
                case ROOK: return BLACK_ROOK;
                case PAWN: return BLACK_PAWN;
            }
        }

        return EMPTY;
    }

}
