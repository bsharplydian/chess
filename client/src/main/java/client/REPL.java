package client;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import websocket.messages.ServerMessage;
import websockethandler.ServerMessageObserver;

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
    public void loadGame(ServerMessage game) {
        GameData gameData = new Gson().fromJson(game.getGame(), GameData.class);
        ChessGame chessGame = gameData.game();
        ChessBoard chessBoard = chessGame.getBoard();
        client.storeChessBoard(chessGame.getBoard());
        String boardOutput = ChessBoardPrinter.displayBoard(chessGame.getBoard(), client.getTeamColor(), null);
        System.out.println("\n" + boardOutput);
        promptUser();
    }

    @Override
    public void showError(ServerMessage error) {
        System.out.println("\n" + SET_TEXT_COLOR_RED + error.getError());
        promptUser();
    }

    private void promptUser() {
        if(client.getLoginStatus() == SIGNEDOUT) {
            System.out.print("\n" + SET_TEXT_COLOR_LIGHT_GREY + RESET_BG_COLOR + "[LOGGED_OUT] >>>  " + SET_TEXT_COLOR_GREEN);
        } else if(client.getLoginStatus() == SIGNEDIN) {
            System.out.print("\n" + SET_TEXT_COLOR_LIGHT_GREY + RESET_BG_COLOR + "[LOGGED_IN] >>>  " + SET_TEXT_COLOR_GREEN);
        } else if(client.getLoginStatus() == PLAYINGGAME) {
            System.out.print("\n" + SET_TEXT_COLOR_LIGHT_GREY + RESET_BG_COLOR + "[PLAYING] >>>  " + SET_TEXT_COLOR_GREEN);
        } else if(client.getLoginStatus() == OBSERVINGGAME) {
            System.out.print("\n" + SET_TEXT_COLOR_LIGHT_GREY + RESET_BG_COLOR + "[OBSERVING] >>>  " + SET_TEXT_COLOR_GREEN);
        }
    }
}
