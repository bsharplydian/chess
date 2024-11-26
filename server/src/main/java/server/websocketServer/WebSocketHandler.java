package server.websocketServer;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;

import static websocket.messages.ServerMessage.ServerMessageType.LOAD_GAME;

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
            case LEAVE -> leaveGame(userGameCommand.getAuthToken(), userGameCommand.getGameID(), userGameCommand.getUserColor(), session);
        }
    }
    private void connectToGame(String authToken, int gameID, String userColor, Session session) throws IOException {
        try {
            UserData userData = dataAccess.getUserByAuth(authToken);
            String username = userData.username();
            var notification = getJoinNotification(username, userColor);

            GameData gameData = dataAccess.getGame(gameID);
            var gameDataMessage = new ServerMessage(LOAD_GAME);
            gameDataMessage.setMessage(new Gson().toJson(gameData));

            connections.addPlayer(username, session);
            connections.loadGameMessage(username, gameDataMessage);
            connections.broadcastExcludeUser(username, notification);
        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
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
        } else {
            message = String.format("error: %s did something that shouldn't be possible", username);
        }
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.setMessage(message);
        return notification;
    }

    private void leaveGame(String authToken, int gameID, String userColor, Session session) throws IOException {
        try {
            UserData userData = dataAccess.getUserByAuth(authToken);
            String username = userData.username();
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
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


}
