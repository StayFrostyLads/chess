package ui;

import chess.*;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

import java.util.Set;

import static ui.EscapeSequences.*;

public class ChessBoardPrinter {

    public static void printBoard(ChessBoard board, boolean whitePerspective) {
        System.out.println();
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
        printFileHeader('a', 'h');
        for (int rank = 8; rank >= 1; rank--) {
            System.out.print(SET_BG_COLOR_BLUE + rank + " " + RESET_BG_COLOR);
            for (int file = 1; file <= 8; file++) {
                printCell(board, rank, file, false);
            }
            System.out.println(SET_BG_COLOR_BLUE + " " + rank + RESET_BG_COLOR);
        }
        printFileFooter('a', 'h');
        System.out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
    }

    /**
     * A helper for printing the board from black's perspective
     */
    public static void printBlackSide(ChessBoard board) {
        printFileHeader('h', 'a');
        for (int rank = 1; rank <= 8; rank++) {
            System.out.print(SET_BG_COLOR_BLUE + rank + " " + RESET_BG_COLOR);
            for (int file = 8; file >= 1; file--) {
                printCell(board, rank, file, false);
            }
            System.out.println(SET_BG_COLOR_BLUE + " " + rank + RESET_BG_COLOR);
        }
        printFileFooter('h', 'a');
        System.out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
    }

    public static void printWithHighlights(ChessBoard board, boolean whitePerspective,
                                           Set<ChessPosition> highlights) {
        System.out.println();
        if (whitePerspective) {
            printFileHeader('a', 'h');
            for (int rank = 8; rank >= 1; rank--) {
                System.out.print(SET_BG_COLOR_BLUE + rank + " " + RESET_BG_COLOR);
                for (int file = 1; file <= 8; file++) {
                    ChessPosition position = new ChessPosition(rank, file);
                    boolean highlight = highlights.contains(position);
                    printCell(board, rank, file, highlight);
                }
                System.out.println(SET_BG_COLOR_BLUE + " " + rank + RESET_BG_COLOR);
            }
            printFileFooter('a', 'h');
        } else {
            printFileHeader('h', 'a');
            for (int rank = 1; rank <= 8; rank++) {
                System.out.print(SET_BG_COLOR_BLUE + rank + " " + RESET_BG_COLOR);
                for (int file = 8; file >= 1; file--) {
                    ChessPosition position = new ChessPosition(rank, file);
                    boolean highlight = highlights.contains(position);
                    printCell(board, rank, file, highlight);
                }
                System.out.println(SET_BG_COLOR_BLUE + " " + rank + RESET_BG_COLOR);
            }
            printFileFooter('h', 'a');
        }
        System.out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
    }

    private static void printCell(ChessBoard board, int rank, int file, boolean highlight) {
        ChessPosition position = new ChessPosition(rank, file);
        ChessPiece piece = board.getPiece(position);

        boolean isLight = ((rank + file) % 2 == 0);
        String background = highlight ? SET_BG_COLOR_YELLOW :
                (isLight ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY);

        String symbol = unicodeForChessPiece(piece);
        String cell = (piece == null) ? "   " : symbol;
        
        System.out.print(background + cell + RESET_BG_COLOR);
    }

    private static void printFileHeader(char startFile, char endFile) {
        System.out.print(SET_BG_COLOR_BLUE + "  ");
        int step = startFile < endFile ? 1 : -1;
        for (char f = startFile; f != (char)(endFile + step); f += (char) step) {
            System.out.print(" " + f + " ");
        }
        System.out.println("  " + RESET_BG_COLOR);
    }

    private static void printFileFooter(char startFile, char endFile) {
        printFileHeader(startFile, endFile);
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
