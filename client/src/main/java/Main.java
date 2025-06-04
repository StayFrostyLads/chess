import chess.*;
import ui.ChessBoardPrinter;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        ChessBoardPrinter.printBoard(board);
    }
}