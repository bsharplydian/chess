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
            case CONNECT -> connect(userGameCommand.getAuthToken(), userGameCommand.getGameID(), session);
        }
    }
    private void connect(String authToken, int gameID, Session session) throws IOException {
        try {
            UserData userData = dataAccess.getUserByAuth(authToken);
            GameData gameData = dataAccess.getGame(gameID);
            String username = userData.username();
            var notification = getPlayerRoleNotification(gameData, username);
            var gameDataMessage = new ServerMessage(LOAD_GAME);
            gameDataMessage.setMessage(new Gson().toJson(gameData));

            connections.addPlayer(username, session);
            connections.loadGameMessage(username, gameDataMessage);
            connections.broadcastExcludeUser(username, notification);
        } catch (DataAccessException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private ServerMessage getPlayerRoleNotification(GameData gameData, String username) {
        String whiteUsername = gameData.whiteUsername();
        String blackUsername = gameData.blackUsername();
        String message;
        if(Objects.equals(username, whiteUsername)) {
            message = String.format("%s is playing as white", username);
        } else if(Objects.equals(username, blackUsername)) {
            message = String.format("%s is playing as black", username);
        } else {
            message = String.format("%s is observing the game", username);
        }
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.setMessage(message);
        return notification;
    }
}
