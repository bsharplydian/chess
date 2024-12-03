package server.websocketserver;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;

import static websocket.messages.ServerMessage.ServerMessageType.*;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final DataAccess dataAccess;
    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
        switch(userGameCommand.getCommandType()) {
            case CONNECT -> connectToGame(userGameCommand.getAuthToken(), userGameCommand.getGameID(), userGameCommand.getUserColor(), session);
            case LEAVE -> leaveGame(userGameCommand.getAuthToken(), userGameCommand.getGameID(), userGameCommand.getUserColor());
            case MAKE_MOVE -> {
                UserMoveCommand userMoveCommand = new Gson().fromJson(message, UserMoveCommand.class);
                makeMove(userMoveCommand.getAuthToken(), userMoveCommand.getGameID(),
                        userMoveCommand.getUserColor(), userMoveCommand.getMove(), session);
            }
            case RESIGN -> resign(userGameCommand.getAuthToken(), userGameCommand.getGameID(), userGameCommand.getUserColor(), session);
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) throws IOException {
        //ignore
    }

    private void connectToGame(String authToken, int gameID, String userColor, Session session) throws IOException {
        try {

            UserData userData = dataAccess.getUserByAuth(authToken);
            if(userData == null) {
                ServerMessage error = new ServerMessage(ERROR);
                error.setError("error: unauthorized");
                session.getRemote().sendString(new Gson().toJson(error));
                return;
            }
            if(dataAccess.getGame(gameID) == null) {
                ServerMessage error = new ServerMessage(ERROR);
                error.setError("error: invalid game id");
                session.getRemote().sendString(new Gson().toJson(error));
                return;
            }
            String username = userData.username();
            userColor = colorLookup(username, gameID, userColor);
            var notification = getJoinNotification(username, userColor);

            GameData gameData = dataAccess.getGame(gameID);
            var gameDataMessage = new ServerMessage(LOAD_GAME);
            gameDataMessage.setGame(new Gson().toJson(gameData));

            connections.addPlayer(gameID, username, session);
            connections.notifySingle(gameID, username, gameDataMessage);
            connections.broadcastExcludeUser(gameID, username, notification);
        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private void leaveGame(String authToken, int gameID, String userColor) throws IOException {
        try {
            UserData userData = dataAccess.getUserByAuth(authToken);
            String username = userData.username();
            userColor = colorLookup(username, gameID, userColor);
            var notification = new ServerMessage(NOTIFICATION);
            notification.setMessage(String.format("%s left the game", username));
            GameData oldGameData = dataAccess.getGame(gameID);
            GameData newGameData = removeColorFromGame(oldGameData, userColor);


            if(Objects.equals(userColor, "WHITE") || Objects.equals(userColor, "BLACK")){
                dataAccess.updateGame(gameID, newGameData);
            }
            connections.removePlayer(gameID, username);
            connections.broadcastExcludeUser(gameID, username, notification);
        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private void makeMove(String authToken, int gameID, String userColor, ChessMove chessMove, Session session) throws IOException {
        //if checkmate/stalemate, notify everyone and update game accordingly
        try {
            UserData userData = dataAccess.getUserByAuth(authToken);
            if(userData == null) { // auth token didn't match with an existing user
                ServerMessage error = new ServerMessage(ERROR);
                error.setError("error: unauthorized");
                session.getRemote().sendString(new Gson().toJson(error));
                return;
            }
            String username = userData.username();
            if(userColor == null) { // the client doesn't provide a color, as in the autograder
                userColor = colorLookup(username, gameID, userColor);
            }
            if(userColor == null) { // after looking up in database, userColor still shows that they are an observer
                ServerMessage error = new ServerMessage(ERROR);
                error.setError("error: observers cannot make moves");
                session.getRemote().sendString(new Gson().toJson(error));
                return;
            }
            ChessGame.TeamColor teamColor = switch(userColor) {
                case "WHITE" -> ChessGame.TeamColor.WHITE;
                case "BLACK" -> ChessGame.TeamColor.BLACK;
                default -> throw new IllegalStateException("Unexpected value: " + userColor);
            };
            ChessGame.TeamColor oppositeTeamColor = switch(teamColor) {
                case WHITE -> ChessGame.TeamColor.BLACK;
                case BLACK -> ChessGame.TeamColor.WHITE;
                default -> throw new IllegalStateException("Unexpected value: " + teamColor);
            };
            //validate move
            GameData oldGameData = dataAccess.getGame(gameID);
            ChessGame chessGame = oldGameData.game();
            if(Objects.equals(chessGame.getTeamTurn(), teamColor)) { // it's the player's turn
                //update game data
                try {
                    chessGame.makeMove(chessMove);
                } catch(InvalidMoveException e) {
                    var error = new ServerMessage(ERROR);
                    error.setError(e.getMessage());
                    connections.notifySingle(gameID, username, error);
                    return;
                }
                GameData newGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(),
                        oldGameData.blackUsername(), oldGameData.gameName(), chessGame);
                dataAccess.updateGame(gameID, newGameData);
                //load game all clients
                var loadGame = new ServerMessage(LOAD_GAME);
                loadGame.setGame(new Gson().toJson(newGameData));
                connections.notifySingle(gameID, username, loadGame);
                connections.broadcastExcludeUser(gameID, username, loadGame);
                //notify all clients
                var notification = new ServerMessage(NOTIFICATION);
                notification.setMessage(String.format("%s: %s", username, chessMove));
                connections.broadcastExcludeUser(gameID, username, notification);

                //test for checkmate
                String opponentUsername = switch(userColor) {
                    case "WHITE" -> newGameData.blackUsername();
                    case "BLACK" -> newGameData.whiteUsername();
                    default -> null;
                };
                if(chessGame.isInCheckmate(oppositeTeamColor)) {
                    notifyAll(gameID, username, String.format("%s is in checkmate. %s wins!", opponentUsername, username));
                    chessGame.setTeamTurn(ChessGame.TeamColor.NONE);
                    GameData concludedGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(),
                            oldGameData.blackUsername(), oldGameData.gameName(), chessGame);
                    dataAccess.updateGame(gameID, concludedGameData);
                } else if(chessGame.isInCheck(oppositeTeamColor)) {
                    notifyAll(gameID, username, String.format("%s(%s) was put in check by %s(%s).", opponentUsername, oppositeTeamColor,
                            username, teamColor));
                } else if(chessGame.isInStalemate(oppositeTeamColor)) {
                    notifyAll(gameID, username, "Stalemate: there is no winner.");
                    chessGame.setTeamTurn(ChessGame.TeamColor.NONE);
                    GameData concludedGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(),
                            oldGameData.blackUsername(), oldGameData.gameName(), chessGame);
                    dataAccess.updateGame(gameID, concludedGameData);
                }
            } else if (Objects.equals(chessGame.getTeamTurn(), ChessGame.TeamColor.NONE)) {
                var error = new ServerMessage(ERROR);
                error.setError("error: the game is over and no more moves can be made");
                connections.notifySingle(gameID, username, error);
            } else {
                var error = new ServerMessage(ERROR);
                error.setError("error: waiting for opponent to play");
                connections.notifySingle(gameID, username, error);
            }
        } catch(DataAccessException e) {
            throw new IOException(e.getMessage());
        }
    }

    private void resign(String authToken, int gameID, String userColor, Session session) throws IOException {
        try {
            UserData userData = dataAccess.getUserByAuth(authToken);
            String username = userData.username();
            if(userColor == null) {
                userColor = colorLookup(username, gameID, userColor);
            }
            if(userColor == null) { // after looking up in database, userColor still shows that they are an observer
                ServerMessage error = new ServerMessage(ERROR);
                error.setError("error: observers cannot make moves");
                session.getRemote().sendString(new Gson().toJson(error));
                return;
            }
            ChessGame.TeamColor teamColor = switch(userColor) {
                case "WHITE" -> ChessGame.TeamColor.WHITE;
                case "BLACK" -> ChessGame.TeamColor.BLACK;
                default -> throw new IllegalStateException("Unexpected value: " + userColor);
            };

            GameData oldGameData = dataAccess.getGame(gameID);
            ChessGame chessGame = oldGameData.game();
            if(chessGame.getTeamTurn() != ChessGame.TeamColor.NONE) {
                notifyAll(gameID, username, String.format("%s has resigned.", username));
                chessGame.setTeamTurn(ChessGame.TeamColor.NONE);
                GameData concludedGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(),
                        oldGameData.blackUsername(), oldGameData.gameName(), chessGame);
                dataAccess.updateGame(gameID, concludedGameData);
            } else {
                ServerMessage error = new ServerMessage(ERROR);
                error.setError("error: game is already over");
                session.getRemote().sendString(new Gson().toJson(error));
            }
        } catch (DataAccessException e) {
            throw new IOException(e.getMessage());
        }
    }

    private GameData removeColorFromGame(GameData oldGameData, String userColor) {
        if(Objects.equals(userColor, "WHITE")) {
            return new GameData(oldGameData.gameID(), null, oldGameData.blackUsername(), oldGameData.gameName(), oldGameData.game());
        } else if (Objects.equals(userColor, "BLACK")) {
            return new GameData(oldGameData.gameID(), oldGameData.whiteUsername(), null, oldGameData.gameName(), oldGameData.game());
        } else {
            return oldGameData;
        }
    }
    private String colorLookup(String username, int gameID, String userColor) throws DataAccessException {
        if(userColor == null) {
            return getColorFromDB(username, gameID);
        } else if(userColor.equals("OBSERVER")) {
            return null;
        } else {
            return userColor;
        }
    }
    private String getColorFromDB(String username, int gameID) throws DataAccessException {
        GameData gameData = dataAccess.getGame(gameID);
        if(Objects.equals(gameData.whiteUsername(), username) && Objects.equals(gameData.blackUsername(), username)) {
            return "BOTH";
        } else if(Objects.equals(gameData.whiteUsername(), username)){
            return "WHITE";
        } else if(Objects.equals(gameData.blackUsername(), username)) {
            return "BLACK";
        } else {
            return null;
        }
    }
    private ServerMessage getJoinNotification(String username, String userColor) {
        String message;
        if(Objects.equals(userColor, "WHITE")) {
            message = String.format("%s is playing as white", username);
        } else if(Objects.equals(userColor, "BLACK")) {
            message = String.format("%s is playing as black", username);
        } else if (Objects.equals(userColor, null)){
            message = String.format("%s is observing the game", username);
        } else if (Objects.equals(userColor, "BOTH")) {
            message = String.format("%s is playing as both colors", username);
        } else {
            message = String.format("error: %s did something that shouldn't be possible", username);
        }
        var notification = new ServerMessage(NOTIFICATION);
        notification.setMessage(message);
        return notification;
    }
    private void notifyAll(int gameID, String username, String message) throws IOException {
        var notification = new ServerMessage(NOTIFICATION);
        notification.setMessage(message);
        connections.notifySingle(gameID, username, notification);
        connections.broadcastExcludeUser(gameID, username, notification);
    }
}
