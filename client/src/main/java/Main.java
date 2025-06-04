import chess.*;
import ui.ChessBoardPrinter;
import client.Repl;


public class Main {
    public static void main(String[] args) {
        String url = "http://localhost:8080";
        if (args.length == 1) {
            url = args[0];
        }
        new Repl(url).run();

//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);
//        ChessBoard board = new ChessBoard();
//        board.resetBoard();
//        ChessBoardPrinter.printBoard(board);
    }
}