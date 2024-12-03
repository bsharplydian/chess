package server.websocketserver;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    /*
    contains logic for keeping connections for multiple games of chess separate from each other
     */
    public final ConcurrentHashMap<Integer, GameManager> games = new ConcurrentHashMap<>();
    public void addGame(int gameID) {
        games.put(gameID, new GameManager());
    }

    public void addPlayer(int gameID, String username, Session session) {
        if(games.get(gameID) == null) {
            addGame(gameID);
        }
        var game = games.get(gameID);
        game.addPlayer(username, session);
    }

    public void removePlayer(int gameID, String username) {
        var game = games.get(gameID);
        game.removePlayer(username);
    }
    public void notifySingle(int gameID, String username, ServerMessage notification) throws IOException {
        var game = games.get(gameID);
        game.notifySingle(username, notification);
    }
    public void broadcastExcludeUser(int gameID, String excludeUsername, ServerMessage notification) throws IOException {
        var game = games.get(gameID);
        game.broadcastExcludeUser(excludeUsername, notification);
    }

}
