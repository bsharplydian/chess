package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.SQLDataAccess;
import handler.ClearHandler;
import handler.GameHandler;
import handler.UserHandler;
import service.UserService;
import service.GameService;
import spark.*;


public class Server {
    private final DataAccess dataAccess = setDataAccess();
    private final UserService userService = new UserService(dataAccess);
    private final UserHandler userHandler = new UserHandler(userService);
    private final GameService gameService = new GameService(dataAccess);
    private final GameHandler gameHandler = new GameHandler(gameService);
    private final ClearHandler clearHandler = new ClearHandler(userService);

    public Server() {

    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::addUser);
        Spark.delete("/db", this::clear);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.get("/game", this::listGames);
        //This line initializes the server and can be removed once you have a functioning endpoint 
//        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
    private DataAccess setDataAccess() {
        try {
            return new SQLDataAccess();
        } catch(DataAccessException e) {
            return new MemoryDataAccess();
        }
    }
    private Object addUser(Request req, Response res) {
        return userHandler.handle(req, res, UserRequestType.REGISTER);
    }
    private Object clear(Request req, Response res) {
        return clearHandler.handle(req, res);
    }
    private Object login(Request req, Response res) {
        return userHandler.handle(req, res, UserRequestType.LOGIN);
    }
    private Object logout(Request req, Response res) {
        return userHandler.handle(req, res, UserRequestType.LOGOUT);
    }
    private Object createGame(Request req, Response res) {
        return gameHandler.handle(req, res, GameRequestType.CREATE);
    }
    private Object joinGame(Request req, Response res) {
        return gameHandler.handle(req, res, GameRequestType.JOIN);
    }
    private Object listGames(Request req, Response res) {
        return gameHandler.handle(req, res, GameRequestType.LIST);
    }
}
