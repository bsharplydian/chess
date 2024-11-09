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
            case "login" -> login(params);
            case "logout" -> logout();
            case "register" -> register(params);
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "play" -> playGame(params);
            case "observe" -> observeGame(params);
            case "quit" -> "quit";
            default -> help();
        };
    }

    public String help() {
        return "this text should appear when the user selects help";
    }
    public String login(String... params) {
        return "";
    }
    public String logout() {
        return "";
    }
    public String register(String... params) {
        return "";
    }
    public String createGame(String... params) {
        return "";
    }
    public String listGames() {
        return "";
    }
    public String playGame(String... params) {
        return "";
    }
    public String observeGame(String... params) {
        return "";
    }
}
