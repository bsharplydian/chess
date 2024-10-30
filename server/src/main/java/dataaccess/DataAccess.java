package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;

public interface DataAccess {
//    AuthData register(UserData user);
    void createUser(UserData userData) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    UserData getUserByAuth(String authToken) throws DataAccessException;
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;

    void deleteAuth(String s) throws DataAccessException;

    int createGame(String s) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    void updateGame(int gameID, GameData gameData) throws DataAccessException;
    ArrayList<GameData> listGames() throws DataAccessException;
}
