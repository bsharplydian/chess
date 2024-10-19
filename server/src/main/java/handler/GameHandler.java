package handler;

import dataaccess.DataAccessException;
import server.RequestType;
import service.GameService;
import spark.Request;
import spark.Response;

public class GameHandler {
    private final GameService gameService;
    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }
//    public Object handle(Request req, Response res, RequestType requestType) throws DataAccessException {
//
//    }
}
