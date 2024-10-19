package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.UserData;
import request.RegisterRequest;
import response.RegisterResponse;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class UserHandler {
    private final UserService userService;
    public UserHandler(UserService userService) {

        this.userService = userService;
    }
    public Object handle(Request req, Response res) throws DataAccessException {
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
        RegisterResponse registerResponse;
        var User = new Gson().fromJson(req.body(), UserData.class); //deserialize json
        RegisterRequest registerRequest = new RegisterRequest(User.username(), User.password(), User.email());

        try {
            registerResponse = userService.register(registerRequest);
        } catch (DataAccessException e) {
            return new Gson().toJson(e.getMessage());
        }
        if(registerResponse.message() != null) {
            if (registerResponse.message().equals("Error: already taken"))
                res.status(403);
            else if(registerResponse.message().equals("Error: bad request"))
                res.status(400);
        }
        return new Gson().toJson(registerResponse);
    }
}
