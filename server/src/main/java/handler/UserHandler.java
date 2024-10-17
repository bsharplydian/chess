package handler;

import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class UserHandler implements Route {
    private final UserService userService;
    UserHandler(UserService userService) {

        this.userService = userService;
    }
    public Object handle(Request req, Response res) {
        //validate auth token
        //deserialize JSON request to java request object
        //call UserService.someMethod & give it java request object
        //serialize java response to JSON
        //send HTTP response back to client
        //receive java
        return 0;
    }
}
