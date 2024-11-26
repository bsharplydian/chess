package client;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import websocketHandler.ServerMessageObserver;

import java.util.Scanner;
import static client.EscapeSequences.*;
import static client.LoginStatus.*;


public class REPL implements ServerMessageObserver {
    private final ChessClient client;

    public REPL(String serverUrl) {
        client = new ChessClient(serverUrl, this);
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

    public void notify(ServerMessage serverMessage) {
        System.out.println("\n"+ SET_TEXT_COLOR_MAGENTA + serverMessage.getMessage());
        promptUser();
    }

    @Override
    public void loadGame(ServerMessage game) { // need to add color logic
        ChessGame chessGame = new Gson().fromJson(game.getMessage(), ChessGame.class);
        client.storeChessBoard(chessGame.getBoard());
        String boardOutput = ChessBoardPrinter.displayBoard(chessGame.getBoard(), client.getTeamColor());
        System.out.println("\n" + boardOutput);
        promptUser();
    }

    @Override
    public void showError(ServerMessage error) {
        System.out.println("\n" + SET_TEXT_COLOR_RED + error.getMessage());
        promptUser();
    }

    private void promptUser() {
        if(client.getLoginStatus() == SIGNEDOUT) {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED_OUT] >>>  " + SET_TEXT_COLOR_GREEN);
        } else if(client.getLoginStatus() == SIGNEDIN) {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED_IN] >>>  " + SET_TEXT_COLOR_GREEN);
        } else if(client.getLoginStatus() == PLAYINGGAME) {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[PLAYING] >>>  " + SET_TEXT_COLOR_GREEN);
        } else if(client.getLoginStatus() == OBSERVINGGAME) {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[OBSERVING] >>>  " + SET_TEXT_COLOR_GREEN);
        }
    }
}
