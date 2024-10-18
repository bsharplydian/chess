package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    Map<String, UserData> users = new HashMap<>();
    Map<String, AuthData> authtokens = new HashMap<>();

    public void createUser(UserData userData) {
        users.put(userData.username(), userData);
    }
    public UserData getUser(String username) {
        return users.get(username);
    }


    public void createAuth(AuthData authData) {
        authtokens.put(authData.authToken(), authData);
    }
    public AuthData getAuth(String authToken) {
        return authtokens.get(authToken);
    }

}
