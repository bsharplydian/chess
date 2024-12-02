package websockethandler;
import websocket.messages.ServerMessage;

public interface ServerMessageObserver {
    void notify(ServerMessage notification);
    void loadGame(ServerMessage game);
    void showError(ServerMessage error);
}
