import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        Server chessServer = new Server();
        chessServer.run(8080);
    }
}