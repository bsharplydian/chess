package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

public class UserService {
    private final DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public AuthData register(UserData user) throws DataAccessException {
        return dataAccess.register(user);
    }
    public Boolean validateAuth(AuthData auth) throws DataAccessException {
        return true;
    }

}
