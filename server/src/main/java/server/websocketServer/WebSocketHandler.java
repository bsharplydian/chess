package server.websocketServer;

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
import java.util.Collection;
import java.util.Objects;

import static websocket.messages.ServerMessage.ServerMessageType.*;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private DataAccess dataAccess;
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
                makeMove(userMoveCommand.getAuthToken(), userMoveCommand.getGameID(), userMoveCommand.getUserColor(), userMoveCommand.getMove());
            }
        }
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
            userColor = handleUserColor(username, gameID, userColor);
            var notification = getJoinNotification(username, userColor);

            GameData gameData = dataAccess.getGame(gameID);
            var gameDataMessage = new ServerMessage(LOAD_GAME);
            gameDataMessage.setGame(new Gson().toJson(gameData));

            connections.addPlayer(username, session);
            connections.loadGameMessage(username, gameDataMessage);
            connections.broadcastExcludeUser(username, notification);
        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
        }
    }
    private String handleUserColor(String username, int gameID, String userColor) throws DataAccessException {
        if(userColor == null) {
            return getColorFromDB(username, gameID);
        } else if(userColor.equals("OBSERVER")) {
            return null;
        } else {
            throw new DataAccessException("invalid user color");
        }
    }
    private String getColorFromDB(String username, int gameID) throws DataAccessException {
        GameData gameData = dataAccess.getGame(gameID);
        if(gameData.whiteUsername().equals(username) && gameData.blackUsername().equals(username)) {
            return "BOTH";
        } else if(gameData.whiteUsername().equals(username)){
            return "WHITE";
        } else if(gameData.blackUsername().equals(username)) {
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

    private void leaveGame(String authToken, int gameID, String userColor) throws IOException {
        try {
            UserData userData = dataAccess.getUserByAuth(authToken);
            String username = userData.username();
            var notification = new ServerMessage(NOTIFICATION);
            notification.setMessage(String.format("%s left the game", username));
            GameData oldGameData = dataAccess.getGame(gameID);
            GameData newGameData = removeColorFromGame(oldGameData, userColor);


            if(Objects.equals(userColor, "WHITE") || Objects.equals(userColor, "BLACK")){
                dataAccess.updateGame(gameID, newGameData);
            }
            connections.removePlayer(username);
            connections.broadcastExcludeUser(username, notification);
        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
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

    private void makeMove(String authToken, int gameID, String userColor, ChessMove chessMove) throws IOException {
        // validate move
            //ensure that it's the correct turn (if the turn is null, that means the game has ended)
        // update game
        // load game all others
        // load game user
        // notify all others

        //if checkmate/stalemate, notify everyone and update game accordingly
        try {

            UserData userData = dataAccess.getUserByAuth(authToken);
            String username = userData.username();
            if(userColor == null) { // the client doesn't provide a color, as in the autograder
                userColor = handleUserColor(username, gameID, userColor);
            }

            //error handling:
            //  user data is null
            //  user color is null
            //  chess move is null
            //  game data is null
            ChessGame.TeamColor teamColor = switch(userColor) {
                case "WHITE" -> ChessGame.TeamColor.WHITE;
                case "BLACK" -> ChessGame.TeamColor.BLACK;
                default -> throw new IllegalStateException("Unexpected value: " + userColor);
            };
            ChessGame.TeamColor oppositeTeamColor = switch(teamColor) {
                case WHITE -> ChessGame.TeamColor.BLACK;
                case BLACK -> ChessGame.TeamColor.WHITE;
            };
            //validate move
            GameData oldGameData = dataAccess.getGame(gameID);
            ChessGame chessGame = oldGameData.game();
            if(Objects.equals(chessGame.getTeamTurn(), teamColor)) { // it's the player's turn
                //update game data
                chessGame.makeMove(chessMove);
                GameData newGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(), oldGameData.blackUsername(), oldGameData.gameName(), chessGame);
                dataAccess.updateGame(gameID, newGameData);
                //load game all clients
                var loadGame = new ServerMessage(LOAD_GAME);
                loadGame.setGame(new Gson().toJson(newGameData));
                connections.notifySingle(username, loadGame);
                connections.broadcastExcludeUser(username, loadGame);
                //notify all clients
                var notification = new ServerMessage(NOTIFICATION);
                notification.setMessage(String.format("%s: %s", username, chessMove));
                connections.broadcastExcludeUser(username, notification);

                //test for checkmate
                if(chessGame.isInCheckmate(oppositeTeamColor)) {
                    notifyAll(username, String.format("Checkmate! %s wins.", username));
                    chessGame.setTeamTurn(null);
                    GameData concludedGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(), oldGameData.blackUsername(), oldGameData.gameName(), chessGame);
                    dataAccess.updateGame(gameID, concludedGameData);
                } else if(chessGame.isInCheck(oppositeTeamColor)) {
                    notifyAll(username, "Check!");
                    chessGame.setTeamTurn(null);
                    GameData concludedGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(), oldGameData.blackUsername(), oldGameData.gameName(), chessGame);
                    dataAccess.updateGame(gameID, concludedGameData);
                } else if(chessGame.isInStalemate(oppositeTeamColor)) {
                    notifyAll(username, "Stalemate: there is no winner.");
                    chessGame.setTeamTurn(null);
                    GameData concludedGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(), oldGameData.blackUsername(), oldGameData.gameName(), chessGame);
                    dataAccess.updateGame(gameID, concludedGameData);
                }
            } else {
                var error = new ServerMessage(ERROR);
                error.setMessage("waiting for opponent to play");
                connections.notifySingle(username, error);
            }
        } catch(DataAccessException | InvalidMoveException e) {
            throw new IOException(e.getMessage());
        }
    }
    private void notifyAll(String username, String message) throws IOException {
        var notification = new ServerMessage(NOTIFICATION);
        notification.setMessage(message);
        connections.notifySingle(username, notification);
        connections.broadcastExcludeUser(username, notification);
    }
}
