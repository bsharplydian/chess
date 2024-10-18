package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    Map<String, UserData> users = new HashMap<>();
    public UserData getUser(String username) {
        return users.get(username);
    }

    public void createUser(UserData userData) {
        users.put(userData.username(), userData);
    }

}
