package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import request.RegisterRequest;
import service.UserService;
import spark.*;

public class Server {
    private final DataAccess dataAccess = new MemoryDataAccess();
    private final UserService userService = new UserService(dataAccess);
    public Server() {

    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::addUser);
        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object addUser(Request req, Response res) {
        var User = new Gson().fromJson(req.body(), UserData.class);
        try {
            userService.register(new RegisterRequest(User.username(), User.password(), User.email()));
        } catch (Exception e) {
            return e.getMessage();
        }
        return new Gson().toJson(User);
    }
}
