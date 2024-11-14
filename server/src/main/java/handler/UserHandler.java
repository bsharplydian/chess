package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import response.LoginResponse;
import response.LogoutResponse;
import response.RegisterResponse;
import server.UserRequestType;
import service.UserService;
import spark.Request;
import spark.Response;

import java.util.Objects;

public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {

        this.userService = userService;
    }

    public Object handle(Request req, Response res, UserRequestType userRequestType){
        //validate auth token if needed for operation
        //deserialize JSON request to java request object
        //call UserService.someMethod & give it java request object
        //serialize java response to JSON
        //send HTTP response back to client
        //receive java

        if (userRequestType == UserRequestType.REGISTER) {
            return registerUser(req, res);
        } else if (userRequestType == UserRequestType.LOGIN) {
            return loginUser(req, res);
        } else if (userRequestType == UserRequestType.LOGOUT) {
            return logoutUser(req, res);
        } else {
            return "{}";
        }
    }

    private Object registerUser(Request req, Response res) {
        RegisterResponse registerResponse;
        RegisterRequest registerRequest = new Gson().fromJson(req.body(), RegisterRequest.class);

        try {
            registerResponse = userService.register(registerRequest);
        } catch (DataAccessException e) {
            registerResponse = new RegisterResponse(null, null, e.getMessage());
        }
        if (Objects.equals(registerResponse.message(), "Error: already taken")) {
            res.status(403);
        } else if (Objects.equals(registerResponse.message(), "Error: bad request")) {
            res.status(400);
        } else if (registerResponse.message() != null) {
            res.status(500);
        }

        return new Gson().toJson(registerResponse);
    }

    private Object loginUser(Request req, Response res) {
        LoginResponse loginResponse;
        LoginRequest loginRequest = new Gson().fromJson(req.body(), LoginRequest.class);
        try {
            loginResponse = userService.login(loginRequest);
        } catch (DataAccessException e) {
            loginResponse = new LoginResponse(null, null, e.getMessage());
        }

        if (Objects.equals(loginResponse.message(), "Error: username or password is incorrect")) {
            res.status(401);
        } else if (loginResponse.message() != null) {
            res.status(500);
        }

        return new Gson().toJson(loginResponse);
    }

    private Object logoutUser(Request req, Response res) {
        LogoutResponse logoutResponse;
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("Authorization"));
        try {
            logoutResponse = userService.logout(logoutRequest);
        } catch (DataAccessException e) {
            logoutResponse = new LogoutResponse(e.getMessage());
        }

        if (Objects.equals(logoutResponse.message(), "Error: unauthorized")) {
            res.status(401);
        } else if (logoutResponse.message() != null) {
            res.status(500);
        }
        return new Gson().toJson(logoutResponse);
    }
}
