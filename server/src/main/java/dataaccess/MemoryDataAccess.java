package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.UUID;

public class MemoryDataAccess implements DataAccess {
    public AuthData register(UserData user) {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());
        user = new UserData(user.username(), user.password(), user.email());

        return auth;
    }
}
