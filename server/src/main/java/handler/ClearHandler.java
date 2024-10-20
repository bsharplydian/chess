package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.RegisterRequest;
import service.GameService;
import service.UserService;
import spark.Request;
import spark.Response;

import java.util.Objects;

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
