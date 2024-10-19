package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.RegisterRequest;
import service.UserService;
import spark.Request;
import spark.Response;

public class ClearHandler {
    private final UserService userService;
    public ClearHandler(UserService userService) {

        this.userService = userService;
    }

    public Object handle(Request req, Response res) throws DataAccessException {
        userService.clear();
        return "{}";
    }
}
