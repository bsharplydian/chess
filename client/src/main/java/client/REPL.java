package client;

import chess.ChessBoard;
import websocket.messages.ServerMessage;
import websocketHandler.ServerMessageObserver;

import java.util.Objects;
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
        System.out.println(SET_TEXT_COLOR_RED + serverMessage.getMessage());
        promptUser();
    }

    private void promptUser() {
        if(client.getLoginStatus() == SIGNEDOUT) {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED_OUT] >>>  " + SET_TEXT_COLOR_GREEN);
        } else {
            System.out.print("\n" + RESET_TEXT_COLOR + RESET_BG_COLOR + "[LOGGED_IN] >>>  " + SET_TEXT_COLOR_GREEN);
        }
    }
}
