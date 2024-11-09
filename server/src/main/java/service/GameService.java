package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.UserData;
import request.CreateRequest;
import request.JoinRequest;
import request.ListRequest;
import response.CreateResponse;
import response.JoinResponse;
import response.ListResponse;

import java.util.ArrayList;
import java.util.Objects;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateResponse createGame(CreateRequest request) throws DataAccessException {
        CreateResponse response;
        if (invalidCreateInput(request)) {
            response = new CreateResponse(null, "Error: bad request");
        } else if (dataAccess.getAuth(request.authToken()) == null) {
            response = new CreateResponse(null, "Error: unauthorized");
        } else {
            String gameID = Integer.toString(dataAccess.createGame(request.gameName()));
            response = new CreateResponse(gameID, null);
        }
        return response;
    }

    private Boolean invalidCreateInput(CreateRequest request) {
        return request.gameName() == null;
    }

    public JoinResponse joinGame(JoinRequest request) throws DataAccessException {
        // for security, rejecting bad requests/unauthorized users should come before manipulating data
        if (invalidJoinInput(request)) {
            return new JoinResponse("Error: bad request");
        } else if (dataAccess.getAuth(request.authToken()) == null) {
            return new JoinResponse("Error: unauthorized");
        }

        JoinResponse response;
        GameData oldGameData = dataAccess.getGame(Integer.parseInt(request.gameID()));
        UserData userData = dataAccess.getUserByAuth(request.authToken());
        GameData newGameData;
        int gameID = oldGameData.gameID();


        if (colorAlreadyExists(request.playerColor(), oldGameData)) {
            response = new JoinResponse("Error: already taken");
        } else {
            newGameData = addUserToGame(request.playerColor(), oldGameData, userData);
            dataAccess.updateGame(gameID, newGameData);
            response = new JoinResponse(null);
        }

        return response;
    }

    private Boolean invalidJoinInput(JoinRequest request) {
        return request.gameID() == null || (!Objects.equals(request.playerColor(), "WHITE") && !Objects.equals(request.playerColor(), "BLACK"));
    }

    private Boolean colorAlreadyExists(String color, GameData gameData) {
        return (Objects.equals(color, "WHITE") && gameData.whiteUsername() != null) ||
                (Objects.equals(color, "BLACK") && gameData.blackUsername() != null);
    }

    private GameData addUserToGame(String color, GameData oldGameData, UserData userData) {
        GameData newGameData;
        if (Objects.equals(color, "WHITE")) {
            newGameData = new GameData(oldGameData.gameID(), userData.username(),
                    oldGameData.blackUsername(), oldGameData.gameName(), oldGameData.game());
        } else {
            newGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(),
                    userData.username(), oldGameData.gameName(), oldGameData.game());
        }
        return newGameData;
    }

    public ListResponse listGames(ListRequest request) throws DataAccessException {
        ListResponse response;
        ArrayList<GameData> gameList;
        if (dataAccess.getAuth(request.authToken()) == null) {
            response = new ListResponse(null, "Error: unauthorized");
        } else {
            gameList = dataAccess.listGames();
            response = new ListResponse(gameList, null);
        }
        return response;
    }
}
