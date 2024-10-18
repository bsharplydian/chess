package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public AuthData register(UserData user) {
        //move logic from dataaccess to service
        if(dataAccess.getUser(user.username()) == null)
            dataAccess.createUser(user);

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(user.username(), token);

        return auth;
    }
    public Boolean validateAuth(AuthData auth) throws DataAccessException {
        return true;
    }

}
