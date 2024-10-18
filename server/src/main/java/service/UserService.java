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
        //move logic from dataaccess to service
        if(dataAccess.getUser(request.username()) == null)
            dataAccess.createUser(new UserData(request.username(), request.password(), request.email()));

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(request.username(), token);

        return new RegisterResponse(request.username(), token);
    }
    public Boolean validateAuth(AuthData auth) throws DataAccessException {
        return true;
    }

}
