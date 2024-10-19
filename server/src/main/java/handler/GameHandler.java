package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.CreateRequest;
import response.CreateResponse;
import server.GameRequestType;
import service.GameService;
import spark.Request;
import spark.Response;

public class GameHandler {
    private final GameService gameService;
    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }
    public Object handle(Request req, Response res, GameRequestType gameRequestType) throws DataAccessException {
        return switch(gameRequestType) {
            case CREATE -> createGame(req, res);
            case LIST -> "list not implemented";
            case JOIN -> "join not implemented";
        };
    }
    /*
    adds the selected header as the first element of the body's json
    */
    private String addHeaderToBody(Request req, String headerObjectSignature, String headerJsonSignature) {
        //String result = "{\"" + req.headers(header) + "\"" + req.body().substring(1);
        String result = String.format("""
        {"%s":"%s", %s
        """, headerObjectSignature, req.headers(headerJsonSignature), req.body().substring(1));
        return result;
    }
    private Object createGame(Request req, Response res) {
        CreateResponse createResponse;
        String HeaderBodyJson = addHeaderToBody(req, "authToken", "Authorization");
        CreateRequest createRequest = new Gson().fromJson(HeaderBodyJson, CreateRequest.class);
        try{
            createResponse = gameService.createGame(createRequest);
        } catch (Exception e) {
            return new Gson().toJson(e.getMessage());
        }

        return new Gson().toJson(createResponse);
    }
}
