package client;

import chess.ChessBoard;

import java.util.Objects;
import java.util.Scanner;
import static client.EscapeSequences.*;


public class REPL {
    private final ChessClient client;

    public REPL(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        var result = "";
        System.out.print("Welcome to chess! HELP to start");

        while(!result.equals("quit")) {
            promptUser();
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                System.out.print(e.toString());
            }
        }
        System.out.println();
    }
    public void displayBoard(ChessBoard board) {
        ChessBoardPrinter.displayBoard(board);
    }
    private void promptUser() {
        System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED_OUT] >>>  " + SET_TEXT_COLOR_GREEN);
    }
}
