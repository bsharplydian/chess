package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import request.RegisterRequest;
import response.RegisterResponse;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public RegisterResponse register(RegisterRequest request) {
        if(dataAccess.getUser(request.username()) == null)
            dataAccess.createUser(new UserData(request.username(), request.password(), request.email()));
        else {
            // throw an already exists error
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(request.username(), token);
        dataAccess.createAuth(auth);
        return new RegisterResponse(request.username(), token, null);
    }
    public Boolean validateAuth(AuthData auth) throws DataAccessException {
        return true;
    }
    public void clear() {
        dataAccess.clear();
    }

}
