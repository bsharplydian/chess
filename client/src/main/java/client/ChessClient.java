package client;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import request.*;
import response.*;
import serverfacade.ServerFacade;
import websocketHandler.ServerMessageObserver;
import websocketHandler.WebsocketClientCommunicator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static client.LoginStatus.*;

public class ChessClient {
    //websocket variables
    private final ServerFacade server;
    private final String serverUrl;
    private LoginStatus loginStatus = SIGNEDOUT;
    private final ServerMessageObserver serverMessageObserver;
    private WebsocketClientCommunicator ws;

    //user data variables
    private String authToken;
    private String username;
    private String teamColor;
    private int currentGameID;

    //game data variables
    private ChessBoard chessBoard;
    private Map<Integer, Integer> gameIDServerKey = new HashMap<>();// key: server id; value: client id
    private final Map<Integer, Integer> gameIDClientKey = new HashMap<>();
    private Map<Integer, String> gameNameClientKey = new HashMap<>();


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
                case "show" -> drawBoard(params);
                case "move" -> makeMove(params);
                case "leave" -> leaveGame(params);
                case "quit" -> quit();
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String quit() throws Exception{
        if(loginStatus == PLAYINGGAME || loginStatus == OBSERVINGGAME) {
            leaveGame();
        }
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
                    \tmove <STARTSQUARE> <ENDSQUARE> [PROMOTION PIECE] - make a chess move. squares are formatted "a1", and promotion piece can be blank
                    \tleave - disconnect (another user could take your place)
                    \tresign - end the game by admitting defeat
                    \tlight <SQUARE> - display the legal moves for a chess piece
                    """;
            case OBSERVINGGAME -> """
                    \tshow - display chess board
                    \tleave - disconnect
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
        else if(loginStatus == PLAYINGGAME || loginStatus == OBSERVINGGAME) {
            return "cannot log out while a game is in session";
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
        } else if(loginStatus == PLAYINGGAME || loginStatus == OBSERVINGGAME){
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
        } else if(loginStatus == PLAYINGGAME || loginStatus == OBSERVINGGAME) {
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
        } else if(loginStatus == PLAYINGGAME || loginStatus == OBSERVINGGAME) {
            return "cannot join a game while a game is already in session";
        }
        if(gameIDClientKey.isEmpty()) {
            return "game may not exist; please list to view available games";
        }
        if(params.length == 2) {
            if(isNumber(params[0]) &&
                    (params[1].equalsIgnoreCase("WHITE") || params[1].equalsIgnoreCase("BLACK"))) {
                int clientID = Integer.parseInt(params[0]);
                if(gameNameClientKey.get(clientID) == null) {
                    return "game may not exist; please list to view available games";
                }

                JoinRequest joinRequest = new JoinRequest(authToken, params[1].toUpperCase(), String.valueOf(gameIDClientKey.get(clientID)));
                server.joinGame(joinRequest);

                ws = new WebsocketClientCommunicator(serverUrl, serverMessageObserver);
                ws.connectToGame(authToken, gameIDClientKey.get(clientID), params[1].toUpperCase());

                this.teamColor = params[1].toUpperCase();
                this.currentGameID = gameIDClientKey.get(clientID);
                loginStatus = PLAYINGGAME;
                return "joined " + gameNameClientKey.get(clientID);
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
        } else if(loginStatus == PLAYINGGAME || loginStatus == OBSERVINGGAME) {
            return "cannot observe a game while a game is already in session";
        }
        if(gameIDClientKey.isEmpty()) {
            return "game may not exist; please list to view available games";
        }
        if(params.length == 1) {
            if(isNumber(params[0])) {
                int clientID = Integer.parseInt(params[0]);
                if(gameNameClientKey.get(clientID) == null) {
                    return "game may not exist; please list to view available games";
                }
                ws = new WebsocketClientCommunicator(serverUrl, serverMessageObserver);
                ws.connectToGame(authToken, gameIDClientKey.get(clientID), "OBSERVER");

                this.currentGameID = gameIDClientKey.get(clientID);
                this.teamColor = "OBSERVER";
                this.loginStatus = OBSERVINGGAME;
                return "observing " + gameNameClientKey.get(clientID);
            }
        }
        return "usage: observe <ID>";
    }

    public String drawBoard(String... params) {
        if(params.length == 0) {
            if(loginStatus == SIGNEDOUT) {
                return "not signed in";
            } else if (loginStatus == SIGNEDIN) {
                return "not in a game";
            } else {
                return ChessBoardPrinter.displayBoard(chessBoard, teamColor);
            }
        }
        return "usage: show does not accept parameters";
    }

    public String leaveGame(String... params) throws Exception {
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        } else if(loginStatus == SIGNEDIN) {
            return "not in a game";
        }
        if(params.length == 0) {
            ws = new WebsocketClientCommunicator(serverUrl, serverMessageObserver);
            ws.leaveGame(authToken, currentGameID, teamColor);

            this.currentGameID = -1;
            this.chessBoard = null;
            this.teamColor = null;
            loginStatus = SIGNEDIN;
            return "left game";
        }
        return "usage: leave does not accept parameters";
    }

    public String makeMove(String... params) throws Exception {
        if(loginStatus == SIGNEDOUT) {
            return "not logged in";
        } else if(loginStatus == SIGNEDIN) {
            return "not in a game";
        } else if(loginStatus == OBSERVINGGAME) {
            return "observers cannot make moves";
        }
        if(params.length == 2 || params.length == 3) {
            ChessMove chessMove;
            if(params.length == 2) {
                chessMove = createChessMove(params[0], params[1], null);
            } else {
                chessMove = createChessMove(params[0], params[1], params[2]);
            }
            if(Objects.equals(chessMove, null)) {
                return "invalid input?";
            }
            ws = new WebsocketClientCommunicator(serverUrl, serverMessageObserver);
            ws.makeMove(authToken, currentGameID, teamColor, chessMove);

            return "attempted to make a move";
        }
        return "usage: move <START> <END> [q|r|b|n|empty]\nSTART and END are formatted \"a1\", promotion piece can be left blank";
    }


    private ChessMove createChessMove(String start, String end, String promotionString){
        try {
            ChessPosition startSquare = getSquare(start);
            ChessPosition endSquare = getSquare(end);
            ChessPiece.PieceType promotionPiece = getPieceType(promotionString);
            return new ChessMove(startSquare, endSquare, promotionPiece);
        } catch (Exception ex) {
            return null;
        }

    }
    private ChessPiece.PieceType getPieceType(String promotionString) {
        if(promotionString == null) {
            return null;
        }
        return switch(promotionString.toLowerCase()) {
            case "q", "queen" -> ChessPiece.PieceType.QUEEN;
            case "r", "rook" -> ChessPiece.PieceType.ROOK;
            case "b", "bishop" -> ChessPiece.PieceType.BISHOP;
            case "n", "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalStateException("Unexpected value: " + promotionString.toLowerCase());
        };
    }
    private ChessPosition getSquare(String square) {
        int row = Integer.parseInt(square.substring(1, 2));
        String colStr = square.substring(0, 1);
        int col = switch(colStr.toLowerCase()) {
            case "a" -> 1;
            case "b" -> 2;
            case "c" -> 3;
            case "d" -> 4;
            case "e" -> 5;
            case "f" -> 6;
            case "g" -> 7;
            case "h" -> 8;
            default -> throw new IllegalStateException("Unexpected value: " + colStr);
        };
        return new ChessPosition(row, col);
    }
    public void storeChessBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public String getTeamColor() {
        return this.teamColor;
    }
    public LoginStatus getLoginStatus() {
        return loginStatus;
    }
}
