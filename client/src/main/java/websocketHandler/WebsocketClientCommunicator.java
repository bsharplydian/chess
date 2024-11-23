package websocketHandler;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebsocketClientCommunicator extends Endpoint {
    Session session;
    ServerMessageObserver serverMessageObserver;
    public WebsocketClientCommunicator(String url, ServerMessageObserver serverMessageObserver) throws Exception{
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.serverMessageObserver = serverMessageObserver;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    switch(serverMessage.getServerMessageType()) {
                        case NOTIFICATION:
                            serverMessageObserver.notify(serverMessage);
                            break;
                        case LOAD_GAME:
                            serverMessageObserver.loadGame(serverMessage);
                            break;
                        case ERROR:
                            break;
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }


    public void connect(String authToken, int gameID, String userColor) throws Exception {
        try {
            var userGameCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, userColor);
            this.session.getBasicRemote().sendText(new Gson().toJson(userGameCommand));

        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }
    }
}
