import chess.*;
import ui.REPL;

public class Main {
    public static void main(String[] args) {

        System.out.println("♕ 240 Chess Client");
        REPL repl = new REPL();
        ChessBoard testBoard = new ChessBoard();
        testBoard.resetBoard();

        repl.run();
    }
}