package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

public interface DataAccess {
//    AuthData register(UserData user);
    void createUser(UserData userData);
    UserData getUser(String username);
    UserData getUserByAuth(String authToken);
    void createAuth(AuthData auth);
    AuthData getAuth(String authToken);
    void clear();

    void deleteAuth(String s);

    int createGame(String s);
    GameData getGame(int gameID);
    void updateGame(int gameID, GameData gameData);
}
