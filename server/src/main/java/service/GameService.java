package service;

import dataaccess.DataAccess;
import request.CreateRequest;
import request.JoinRequest;
import response.CreateResponse;
import response.JoinResponse;

import java.util.Random;
import java.util.random.RandomGenerator;

public class GameService {
    private final DataAccess dataAccess;
    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateResponse createGame(CreateRequest request) {
        CreateResponse response;
        if(invalidCreateInput(request))
            response = new CreateResponse(null, "Error: bad request");
        else if(dataAccess.getAuth(request.authToken()) == null)
            response = new CreateResponse(null, "Error: unauthorized");
        else {
            String gameID = Integer.toString(dataAccess.createGame(request.gameName()));
            response = new CreateResponse(gameID, null);
        }
        return response;
    }
    private Boolean invalidCreateInput(CreateRequest request) {
        return request.gameName() == null;
    }

    public JoinResponse joinGame(JoinRequest request) {
        JoinResponse response;
        if(invalidJoinInput(request))
            response = new JoinResponse("Error:bad request");
        else if(dataAccess.getAuth(request.authToken()) == null)
            response = new JoinResponse("Error: unauthorized");
        else {
            response = new JoinResponse(null);
        }

        return response;
    }
    private Boolean invalidJoinInput(JoinRequest request) {
        return request.gameID() == null || request.playerColor() == null;
    }
}
