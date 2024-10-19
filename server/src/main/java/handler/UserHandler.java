package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.UserData;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import response.LoginResponse;
import response.LogoutResponse;
import response.RegisterResponse;
import server.RequestType;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Objects;

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

        if(requestType == RequestType.REGISTER) {
            return registerUser(req, res);
        } else if (requestType == RequestType.LOGIN) {
            return loginUser(req, res);
        } else if (requestType == RequestType.LOGOUT) {
            return logoutUser(req, res);
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
        if (Objects.equals(registerResponse.message(), "Error: already taken"))
            res.status(403);
        else if (Objects.equals(registerResponse.message(), "Error: bad request"))
            res.status(400);
        else if (registerResponse.message() != null)
            res.status(500);

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
        if(Objects.equals(loginResponse.message(), "Error: unauthorized"))
            res.status(401);
        else if (loginResponse.message() != null)
            res.status(500);

        return new Gson().toJson(loginResponse);
    }
    private Object logoutUser(Request req, Response res) {
        LogoutResponse logoutResponse;
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("Authorization"));
        //Gson().fromJson("{\"AuthToken\":\"" + req.headers("Authorization") + "\"}", LogoutRequest.class);
        try {
            logoutResponse = userService.logout(logoutRequest);
        } catch (DataAccessException e) {
            return new Gson().toJson(e.getMessage());
        }
        if(Objects.equals(logoutResponse.message(), "Error: unauthorized"))
            res.status(401);
        else if (logoutResponse.message() != null)
            res.status(500);

        return new Gson().toJson(logoutResponse);
    }
}
