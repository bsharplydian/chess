package client;

import chess.ChessBoard;
import serverfacade.ServerFacade;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help"; //default to help command if no input
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch(cmd) {
            case "display" -> ChessBoardPrinter.displayBoard(new ChessBoard());
            case "quit" -> "quit";
            default -> throw new IllegalStateException("Unexpected value: " + cmd);
        };
    }
}
