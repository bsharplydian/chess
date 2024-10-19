package service;

import dataaccess.DataAccess;
import request.CreateRequest;
import response.CreateResponse;

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
}
