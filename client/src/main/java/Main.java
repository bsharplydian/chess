import chess.*;
import ui.UI;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        UI ui = new UI();
        ChessBoard testBoard = new ChessBoard();
        testBoard.resetBoard();
        ui.displayBoard(testBoard);
    }
}