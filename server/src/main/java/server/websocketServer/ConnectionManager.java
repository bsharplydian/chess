package server.websocketServer;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, GameManager> games = new ConcurrentHashMap<>();

}
