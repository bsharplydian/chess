package client;

import chess.ChessBoard;
import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import request.*;
import response.*;
import serverfacade.ServerFacade;

import java.util.Arrays;

import static client.LoginStatus.*;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private LoginStatus loginStatus = SIGNEDOUT;
    private String authToken;
    private String username;

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
                case "quit" -> quit();
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String quit() throws Exception{
        if (loginStatus == SIGNEDIN) {
            logout();
        }
        return "quit";
    }
    public String help() {
        if(loginStatus == SIGNEDOUT) {
            return """  
                        \tregister <USERNAME> <PASSWORD> <EMAIL> - create an account
                        \tlogin <USERNAME> <PASSWORD> - log in to an account
                        \tquit - close the program
                        \thelp - display help menu""";
        }
        return """
                \tcreate <NAME> - create a new game
                \tlist - list games
                \tjoin <ID> [WHITE|BLACK] - join an existing game as a given color
                \tobserve <ID> - observe an existing game
                \tlogout - log out
                \tquit - close the program
                \thelp - display help menu""";
    }
    public String login(String... params) throws Exception {
        if(loginStatus == SIGNEDIN){
            return "already logged in";
        }

        if(params.length == 2) {
            LoginRequest loginRequest = new LoginRequest(params[0], params[1]);
            LoginResponse loginResponse = server.login(loginRequest);
            if(loginResponse.message() == null){
                loginStatus = SIGNEDIN;
                authToken = loginResponse.authToken();
                return "logged in as " + loginResponse.username() + " " + new Gson().toJson(loginResponse);
            } else {
                return loginResponse.message();
            }

        }
        return "usage: login <USERNAME> <PASSWORD>";
    }
    public String logout() throws Exception {
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        }
        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        LogoutResponse logoutResponse = server.logout(logoutRequest);
        if(logoutResponse.message() == null) {
            loginStatus = SIGNEDOUT;
            return "successfully logged out";
        } else {
            return logoutResponse.message();
        }
    }
    public String register(String... params) throws Exception {
        if(loginStatus == SIGNEDIN) {
            return "already logged in";
        }

        if(params.length == 3) {
            loginStatus = LoginStatus.SIGNEDIN;
            RegisterRequest registerRequest = new RegisterRequest(params[0], params[1], params[2]);
            RegisterResponse registerResponse = server.addUser(registerRequest);
            return "registered as " + registerRequest.username() + " " + new Gson().toJson(registerResponse);
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
