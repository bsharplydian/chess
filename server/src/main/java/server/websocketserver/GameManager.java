package server.websocketserver;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    /*
    contains logic for handling connections in a single game
     */
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void addPlayer(String username, Session session) {
        var connection = new Connection(username, session);

        connections.put(username, connection);
    }

    public void removePlayer(String username) {
        connections.remove(username);
    }
    public void notifySingle(String username, ServerMessage notification) throws IOException {
        var connection = connections.get(username);
        if(connection.session.isOpen()) {
            connection.send(notification.toString());
        } else {
            connections.remove(connection.username);
        }
    }
    public void broadcastExcludeUser(String excludeUsername, ServerMessage notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for(var c : connections.values()) {
            if(c.session.isOpen()) {
                if(!c.username.equals(excludeUsername)) {
                    c.send(notification.toString());
                }
            } else {
                removeList.add(c);
            }
        }
        for (var c : removeList) {
            connections.remove(c.username);
        }
    }
}
