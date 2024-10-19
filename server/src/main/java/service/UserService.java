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

    public RegisterResponse register(RegisterRequest request) throws DataAccessException {
        RegisterResponse response;
        if(invalidRegisterInput(request))
            response = new RegisterResponse(null, null, "Error: bad request");
        else if(dataAccess.getUser(request.username()) != null) {
            response = new RegisterResponse(null, null, "Error: already taken");
        }

        else { // no match -> register given user
            dataAccess.createUser(new UserData(request.username(), request.password(), request.email()));

            String token = UUID.randomUUID().toString();
            AuthData auth = new AuthData(request.username(), token);
            dataAccess.createAuth(auth);
            response = new RegisterResponse(request.username(), token, null);
        }


        
        return response;
    }
    private Boolean invalidRegisterInput(RegisterRequest request) {
        return request.username() == null || request.email() == null || request.password() == null;
    }
    private Boolean validateAuth(AuthData auth) throws DataAccessException {
        return true;
    }
    public void clear() {
        dataAccess.clear();
    }

}
