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
    public Object handle(Request req, Response res) {
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
        var User = new Gson().fromJson(req.body(), UserData.class);
        try {
            registerResponse = userService.register(new RegisterRequest(User.username(), User.password(), User.email()));
        } catch (Exception e) {
            return new Gson().toJson("""
                    "error": "happened"
                    """);
        }
        return new Gson().toJson(registerResponse);
    }
}
