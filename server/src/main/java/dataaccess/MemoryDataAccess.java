package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    Map<String, UserData> users = new HashMap<>();
    Map<String, AuthData> authtokens = new HashMap<>();
    Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    public void createUser(UserData userData) {
        users.put(userData.username(), userData);
    }
    public UserData getUser(String username) {
        return users.get(username);
    }
    public UserData getUserByAuth(String authToken) {
        AuthData authData = getAuth(authToken);
        if(authData == null)
            return null;
        else
            return getUser(authData.username());
    }

    public void createAuth(AuthData authData) {
        authtokens.put(authData.authToken(), authData);
    }
    public AuthData getAuth(String authToken) {
        return authtokens.get(authToken);
    }
    public void deleteAuth(String authToken) {
        authtokens.remove(authToken);
    }

    public int createGame(String gameName) {
        int gameID = nextGameID++;
        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, gameName, chessGame);
        games.put(gameID, gameData);
        return gameID;
    }
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    public void updateGame(int gameID, GameData gameData) {
        games.put(gameID, gameData);
    }

    public ArrayList<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    public void clear() {
        users.clear();
        authtokens.clear();
    }

}
