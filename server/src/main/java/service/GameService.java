package service;

import dataaccess.DataAccess;
import request.CreateRequest;
import response.CreateResponse;

public class GameService {
    public GameService(DataAccess dataAccess) {

    }

    public CreateResponse createGame(CreateRequest createRequest) {
        CreateResponse response;
        response = new CreateResponse(1234, null);
        return response;
    }
}
