package service;

import dataaccess.DataAccess;
import model.GameData;
import model.UserData;
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
        GameData oldGameData = dataAccess.getGame(Integer.parseInt(request.gameID()));
        UserData userData = dataAccess.getUserByAuth(request.authToken());
        GameData newGameData;
        int gameID = oldGameData.gameID();
        if(invalidJoinInput(request))
            response = new JoinResponse("Error: bad request");
        else if(dataAccess.getAuth(request.authToken()) == null)
            response = new JoinResponse("Error: unauthorized");
        else if(colorAlreadyExists(request.playerColor(), oldGameData))
            response = new JoinResponse("Error: already taken");

        else {
            newGameData = addUserToGame(request.playerColor(), oldGameData, userData);
            dataAccess.updateGame(gameID, newGameData);
            response = new JoinResponse(null);
        }

        return response;
    }
    private Boolean invalidJoinInput(JoinRequest request) {
        return request.gameID() == null || (!request.playerColor().equals("WHITE") && !request.playerColor().equals("BLACK"));
    }
    private Boolean colorAlreadyExists(String color, GameData gameData) {
        return (color.equals("WHITE") && gameData.whiteUsername() != null) ||
                (color.equals("BLACK") && gameData.blackUsername() != null);
    }
    private GameData addUserToGame(String color, GameData oldGameData, UserData userData) {
        GameData newGameData;
        if(color.equals("WHITE")) {
            newGameData = new GameData(oldGameData.gameID(), userData.username(),
                    oldGameData.blackUsername(), oldGameData.gameName(), oldGameData.game());
        } else {
            newGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(),
                    userData.username(), oldGameData.gameName(), oldGameData.game());
        }
        return newGameData;
    }
}
