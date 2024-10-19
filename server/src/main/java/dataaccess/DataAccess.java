package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

public interface DataAccess {
//    AuthData register(UserData user);
    void createUser(UserData userData);
    UserData getUser(String username);
    void createAuth(AuthData auth);
    AuthData getAuth(String authToken);
    void clear();

    void deleteAuth(String s);

    int createGame(String s);
    GameData getGame(int gameID);
}
