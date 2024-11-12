package client;

import chess.ChessBoard;
import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import request.*;
import response.*;
import serverfacade.ServerFacade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static client.LoginStatus.*;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private LoginStatus loginStatus = SIGNEDOUT;
    private String authToken;
    private String username;
    private ArrayList<Integer> gameIDs = new ArrayList<>();
    private Map<Integer, Integer> gameIDServerKey;// key: server id; value: client id
    private Map<Integer, Integer> gameIDClientKey;
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

    public String logout(String... params) throws Exception {
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        }
        if(params.length == 0) {
            LogoutRequest logoutRequest = new LogoutRequest(authToken);
            LogoutResponse logoutResponse = server.logout(logoutRequest);
            if (logoutResponse.message() == null) {
                loginStatus = SIGNEDOUT;
                return "successfully logged out";
            } else {
                return logoutResponse.message();
            }
        }
        return "error: logout does not accept parameters";
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
        return "error\nusage: register <USERNAME> <PASSWORD> <EMAIL>";
    }

    public String createGame(String... params) throws Exception{
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        }
        if(params.length == 1) {
            CreateRequest createRequest = new CreateRequest(authToken, params[0]);
            CreateResponse createResponse = server.createGame(createRequest);
            return "created game " + createRequest.gameName() + " at server id " + createResponse.gameID();
        }
        return "error\nusage: create <NAME>";
    }

    public String listGames(String... params) throws Exception {
        gameIDs.clear();
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        }
        if(params.length == 0) {
            ListRequest listRequest = new ListRequest(authToken);
            ListResponse listResponse = server.listGames(listRequest);
            for(var game : listResponse.games()) {
                gameIDs.add(game.gameID());
            }
        }
        return gameIDs.toString();
    }
    public String playGame(String... params) {
        return "play not implemented";
    }
    public String observeGame(String... params) {
        return "observe not implemented";
    }

    public LoginStatus getLoginStatus() {
        return loginStatus;
    }
}
