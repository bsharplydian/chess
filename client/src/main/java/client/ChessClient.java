package client;

import chess.ChessBoard;
import request.*;
import response.*;
import serverfacade.ServerFacade;
import websocketHandler.ServerMessageObserver;
import websocketHandler.WebsocketClientCommunicator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static client.LoginStatus.*;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private LoginStatus loginStatus = SIGNEDOUT;
    private String authToken;
    private String username;
    private Map<Integer, Integer> gameIDServerKey = new HashMap<>();// key: server id; value: client id
    private final Map<Integer, Integer> gameIDClientKey = new HashMap<>();
    private Map<Integer, String> gameNameClientKey = new HashMap<>();
    private final ServerMessageObserver serverMessageObserver;
    private WebsocketClientCommunicator ws;

    public ChessClient(String serverUrl, ServerMessageObserver serverMessageObserver) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.serverMessageObserver = serverMessageObserver;
    }

    public String eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help"; //default to help command if no input
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "logout" -> logout(params);
                case "register" -> register(params);
                case "create" -> createGame(params);
                case "list" -> listGames(params);
                case "join" -> joinGame(params);
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
        return switch(loginStatus) {
            case SIGNEDOUT -> """  
                        \tregister <USERNAME> <PASSWORD> <EMAIL> - create an account
                        \tlogin <USERNAME> <PASSWORD> - log in to an account
                        \tquit - close the program
                        \thelp - display help menu""";
            case SIGNEDIN -> """
                    \tcreate <NAME> - create a new game
                    \tlist - list games
                    \tjoin <ID> [WHITE|BLACK] - join an existing game as a given color
                    \tobserve <ID> - observe an existing game
                    \tlogout - log out
                    \tquit - close the program
                    \thelp - display help menu""";
            case PLAYINGGAME -> """
                    \tshow - display chess board
                    \tmove <STARTSQUARE> <ENDSQUARE> - make a chess move. squares are formatted "a1"
                    \tleave - disconnect (another user could take your place)
                    \tresign - end the game by admitting defeat
                    \tlight <SQUARE> - display the legal moves for a chess piece
                    """;
        };
    }

    public String login(String... params) throws Exception {
        if(loginStatus != SIGNEDOUT){
            return "already logged in";
        }
        if(params.length == 2) {
            LoginRequest loginRequest = new LoginRequest(params[0], params[1]);
            LoginResponse loginResponse = server.login(loginRequest);
            if(loginResponse.message() == null){
                loginStatus = SIGNEDIN;
                authToken = loginResponse.authToken();
                return "logged in as " + loginResponse.username();
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
        else if(loginStatus == PLAYINGGAME) {
            return "cannot log out while a game is in session\nplease leave this game before logging out";
        }
        if(params.length == 0) {
            LogoutRequest logoutRequest = new LogoutRequest(authToken);
            LogoutResponse logoutResponse = server.logout(logoutRequest);
            if (logoutResponse.message() == null) {
                loginStatus = SIGNEDOUT;
                gameIDClientKey.clear();
                gameIDServerKey.clear();
                gameNameClientKey.clear();
                return "successfully logged out";
            } else {
                return logoutResponse.message();
            }
        }
        return "error\nusage: logout does not accept parameters";
    }

    public String register(String... params) throws Exception {
        if(loginStatus != SIGNEDOUT) {
            return "already logged in: new user was not registered";
        }
        if(params.length == 3) {
            RegisterRequest registerRequest = new RegisterRequest(params[0], params[1], params[2]);
            RegisterResponse registerResponse = server.addUser(registerRequest);
            authToken = registerResponse.authToken();
            if(authToken != null) {
                loginStatus = SIGNEDIN;
            }
            return "registered as " + registerRequest.username();
        }
        return "error\nusage: register <USERNAME> <PASSWORD> <EMAIL>";
    }

    public String createGame(String... params) throws Exception{
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        } else if(loginStatus == PLAYINGGAME){
            return "cannot create a game while a game is in session";
        }
        if(params.length == 1) {
            CreateRequest createRequest = new CreateRequest(authToken, params[0]);
            server.createGame(createRequest);
            return "created game " + createRequest.gameName();
        }
        return "error\nusage: create <NAME>";
    }

    public String listGames(String... params) throws Exception {
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        } else if(loginStatus == PLAYINGGAME) {
            return "cannot list games while a game is in session";
        }
        StringBuilder listBuilder = new StringBuilder();
        int gameCounter = 0;
        if(params.length == 0) {
            ListRequest listRequest = new ListRequest(authToken);
            ListResponse listResponse = server.listGames(listRequest);
            if(listResponse.games().isEmpty()) {
                return "no games to display";
            }
            for(var game : listResponse.games()) {
                listBuilder.append(String.format("%d. %s\n\tWhite: %s\n\tBlack: %s\n",
                        ++gameCounter, game.gameName(), game.whiteUsername(), game.blackUsername()));
                gameIDServerKey.put(game.gameID(), gameCounter);
                gameIDClientKey.put(gameCounter, game.gameID());
                gameNameClientKey.put(gameCounter, game.gameName());
            }
            return listBuilder.toString();
        }

        return "error\nusage: list does not accept parameters";

    }

    public String joinGame(String... params) throws Exception {
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        } else if(loginStatus == PLAYINGGAME) {
            return "cannot join a game while a game is already in session";
        }
        if(gameIDClientKey.isEmpty()) {
            return "please list games to confirm ID before joining";
        }
        if(params.length == 2) {
            if(isNumber(params[0]) &&
                    (params[1].equalsIgnoreCase("WHITE") || params[1].equalsIgnoreCase("BLACK"))) {
                int clientID = Integer.parseInt(params[0]);
                if(gameNameClientKey.get(clientID) == null) {
                    return "game does not exist";
                }

                JoinRequest joinRequest = new JoinRequest(authToken, params[1].toUpperCase(), String.valueOf(gameIDClientKey.get(clientID)));
                server.joinGame(joinRequest);
                ws = new WebsocketClientCommunicator(serverUrl, serverMessageObserver);
                ws.connect(authToken, gameIDClientKey.get(clientID));
                ChessBoard board = new ChessBoard();
                board.resetBoard();
                return "joined game " + gameNameClientKey.get(clientID) + "\n" + ChessBoardPrinter.displayBoard(board);
            }
        }
        return "usage: join <ID> [WHITE|BLACK]";
    }

    private boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String observeGame(String... params) throws Exception {
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        } else if(loginStatus == PLAYINGGAME) {
            return "cannot observe a game while a game is already in session";
        }
        if(gameIDClientKey.isEmpty()) {
            return "please list games to confirm ID before joining";
        }
        if(params.length == 1) {
            if(isNumber(params[0])) {
                int clientID = Integer.parseInt(params[0]);
                ChessBoard board = new ChessBoard();
                board.resetBoard();
                if(gameNameClientKey.get(clientID) == null) {
                    return "game does not exist";
                }
                return "observing game " + gameNameClientKey.get(clientID) + "\n" + ChessBoardPrinter.displayBoard(board);
            }
        }
        return "usage: observe <ID>";
    }

    public LoginStatus getLoginStatus() {
        return loginStatus;
    }
}
