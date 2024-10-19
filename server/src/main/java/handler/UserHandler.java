package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.UserData;
import request.LoginRequest;
import request.RegisterRequest;
import response.LoginResponse;
import response.RegisterResponse;
import server.RequestType;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class UserHandler {
    private final UserService userService;
    public UserHandler(UserService userService) {

        this.userService = userService;
    }
    public Object handle(Request req, Response res, RequestType requestType) throws DataAccessException {
        //validate auth token if needed for operation
        //deserialize JSON request to java request object
        //call UserService.someMethod & give it java request object
        //serialize java response to JSON
        //send HTTP response back to client
        //receive java

        /* actual to do list:
        -add logic to determine WHAT service method needs to be called (register, login, logout)
        -move try-catch block into Server class (where it says) (also do this for ClearHandler)
         */
        if(requestType == RequestType.REGISTER) {
            return registerUser(req, res);
        } else if (requestType == RequestType.LOGIN) {
            return loginUser(req, res);
        }
        else return "{}";
    }
    private Object registerUser(Request req, Response res) {
        RegisterResponse registerResponse;
        RegisterRequest registerRequest = new Gson().fromJson(req.body(), RegisterRequest.class);

        try {
            registerResponse = userService.register(registerRequest);
        } catch (DataAccessException e) {
            return new Gson().toJson(e.getMessage());
        }
        if (registerResponse.message() != null) {
            if (registerResponse.message().equals("Error: already taken"))
                res.status(403);
            else if (registerResponse.message().equals("Error: bad request"))
                res.status(400);
        }
        return new Gson().toJson(registerResponse);
    }
    private Object loginUser(Request req, Response res) {
        LoginResponse loginResponse;
        LoginRequest loginRequest = new Gson().fromJson(req.body(), LoginRequest.class);
        try {
            loginResponse = userService.login(loginRequest);
        } catch (DataAccessException e) {
            return new Gson().toJson(e.getMessage());
        }
        return new Gson().toJson(loginResponse);
    }
}
