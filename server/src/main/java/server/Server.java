package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import handler.ClearHandler;
import handler.UserHandler;
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
        try {
            Spark.post("/user", this::addUser);
            Spark.delete("/db", this::clear);
            Spark.post("/session", this::login);
        } catch (Exception e) {

        }
        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object addUser(Request req, Response res) throws DataAccessException {
        UserHandler userHandler = new UserHandler(userService);
        return userHandler.handle(req, res, RequestType.REGISTER);
    }
    private Object clear(Request req, Response res) throws DataAccessException {
        ClearHandler clearHandler = new ClearHandler(userService);
        return clearHandler.handle(req, res);
    }
    private Object login(Request req, Response res) throws DataAccessException {
        UserHandler userHandler = new UserHandler(userService);
        return userHandler.handle(req, res, RequestType.LOGIN);
    }
}
