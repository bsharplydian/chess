package ui;

import chess.ChessBoard;

import java.util.Objects;
import java.util.Scanner;
import static ui.EscapeSequences.*;


public class REPL {

    public void run() {
        Scanner scanner = new Scanner(System.in);
        var result = "";
        System.out.print("Welcome to chess! HELP to start");
        while(!result.equals("quit")) {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED_OUT] >>>  " + SET_TEXT_COLOR_GREEN);
            String line = scanner.nextLine();
            if(Objects.equals(line, "display")) {
                ChessBoard testBoard = new ChessBoard();
                testBoard.resetBoard();
                displayBoard(testBoard);
            }
            if(Objects.equals(line, "quit")) {
                result = "quit";
            }
            System.out.print(RESET_TEXT_COLOR);
            System.out.print("congrats, you typed \"" + line + "\"");
        }
        System.out.println();
    }
    public void displayBoard(ChessBoard board) {
        ChessBoardPrinter.displayBoard(board);
    }
}
