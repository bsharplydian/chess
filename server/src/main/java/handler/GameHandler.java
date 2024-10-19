package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.CreateRequest;
import request.JoinRequest;
import response.CreateResponse;
import response.JoinResponse;
import server.GameRequestType;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.Objects;

public class GameHandler {
    private final GameService gameService;
    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }
    public Object handle(Request req, Response res, GameRequestType gameRequestType) throws DataAccessException {
        return switch(gameRequestType) {
            case CREATE -> createGame(req, res);
            case LIST -> "list not implemented";
            case JOIN -> joinGame(req, res);
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
        if(Objects.equals(createResponse.message(), "Error: unauthorized"))
            res.status(401);
        else if (createResponse.message() != null)
            res.status(500);
        return new Gson().toJson(createResponse);
    }

    private Object joinGame(Request req, Response res) {
        JoinResponse joinResponse;
        String HeaderBodyJson = addHeaderToBody(req, "authToken", "Authorization");
        JoinRequest joinRequest = new Gson().fromJson(HeaderBodyJson, JoinRequest.class);
        try {
            joinResponse = gameService.joinGame(joinRequest);
        } catch (Exception e) {
            return new Gson().toJson(e.getMessage());
        }
        if(Objects.equals(joinResponse.message(), "Error: bad request"))
            res.status(400);
        else if(Objects.equals(joinResponse.message(), "Error: unauthorized"))
            res.status(401);
        else if(Objects.equals(joinResponse.message(), "Error: already taken"))
            res.status(403);
        else if(joinResponse.message() != null)
            res.status(500);
        return new Gson().toJson(joinResponse);
    }

    private Object listGames(Request req, Response res) {

    }
}
