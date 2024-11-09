package client;

import chess.ChessBoard;
import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import request.*;
import response.*;
import serverfacade.ServerFacade;

import java.util.Arrays;

import static client.LoginStatus.SIGNEDOUT;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private LoginStatus loginStatus = SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help"; //default to help command if no input
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
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
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        if(loginStatus == SIGNEDOUT) {
            return """  
                        \tregister <USERNAME> <PASSWORD> <EMAIL> - create an account
                        \tlogin <USERNAME> <PASSWORD> - log in to an account
                        \tquit - close the program
                        \thelp - display help menu""";
        }
        return "";
    }
    public String login(String... params) {
        return "login not implemented";
    }
    public String logout() {
        return "logout not implemented";
    }
    public String register(String... params) throws Exception {
        if(params.length == 3) {
            loginStatus = LoginStatus.SIGNEDIN;
            RegisterRequest newUser = new RegisterRequest(params[0], params[1], params[2]);
            RegisterResponse responseData = server.addUser(newUser);
            return "registered as " + newUser.username() + " " + new Gson().toJson(responseData);
        }
        return "usage: register <USERNAME> <PASSWORD> <EMAIL>";
    }
    public String createGame(String... params) {
        return "create not implemented";
    }
    public String listGames() {
        return "list not implemented";
    }
    public String playGame(String... params) {
        return "play not implemented";
    }
    public String observeGame(String... params) {
        return "observe not implemented";
    }
}
