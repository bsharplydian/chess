package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import request.*;
import response.*;


public class ServiceTests {
    private static MemoryDataAccess db;
    private static UserService userService;
    private static GameService gameService;
    private static UserData newUser;
    private static GameData newGame;

    @BeforeAll
    public static void init() {
        db = new MemoryDataAccess();
        userService = new UserService(db);
        gameService = new GameService(db);
        newUser = new UserData("james", "12345", "james@mynameisjames.com");
    }
    @Test
    public void register() throws DataAccessException {
        var registerRes = userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        Assertions.assertEquals(newUser, db.getUser("james"));
        Assertions.assertNotNull(db.getAuth(registerRes.authToken()));
    }
    @Test
    public void registerAndCheckAuth() throws DataAccessException {
        var registerResult = userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        AuthData authData = new AuthData(registerResult.username(), registerResult.authToken());
        Assertions.assertEquals(36, authData.authToken().length());
        Assertions.assertEquals("james", authData.username());

        //this test doesn't do anything
    }

    @Test
    public void loginSuccess() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        Assertions.assertNull(loginResponse.message());
    }
    @Test
    public void loginFailDoesntExist() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        LoginRequest loginRequest = new LoginRequest("patrick", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        Assertions.assertNotNull(loginResponse.message());
    }

    @Test
    public void loginFailWrongPassword() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        LoginRequest loginRequest = new LoginRequest("james", "01234");
        LoginResponse loginResponse = userService.login(loginRequest);
        Assertions.assertNotNull(loginResponse.message());
    }

    @Test
    public void logoutSuccess() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);

        LogoutRequest logoutRequest = new LogoutRequest(loginResponse.authToken());
        LogoutResponse logoutResponse = userService.logout(logoutRequest);
        Assertions.assertNull(logoutResponse.message());
        Assertions.assertNull(db.getAuth(loginResponse.authToken()));
    }

    @Test
    @Disabled
    public void logoutFail() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);

        LogoutRequest logoutRequest = new LogoutRequest(loginResponse.authToken());
        LogoutResponse logoutResponse = userService.logout(logoutRequest);
        Assertions.assertNotNull(logoutResponse.message());
        Assertions.assertNotNull(db.getAuth(loginResponse.authToken()));
    }
    @Test
    public void createSuccess() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);

        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), "myGame");
        CreateResponse createResponse = gameService.createGame(createRequest);
        Assertions.assertNotEquals(createResponse.gameID(), null);
        Assertions.assertNotNull(db.getGame(Integer.parseInt(createResponse.gameID())));
    }

    @Test
    public void joinSuccess() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), "myGame");
        CreateResponse createResponse = gameService.createGame(createRequest);

        String gameID = createResponse.gameID();
        JoinRequest joinRequest = new JoinRequest(loginResponse.authToken(), "WHITE", gameID);
        JoinResponse joinResponse = gameService.joinGame(joinRequest);
        Assertions.assertNull(joinResponse.message());
    }

    @Test
    public void listSuccess() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), "myGame");
        CreateResponse createResponse = gameService.createGame(createRequest);

        ListRequest listRequest = new ListRequest(loginResponse.authToken());
        ListResponse listResponse = gameService.listGames(listRequest);
        Assertions.assertNull(listResponse.message());
    }
    @Test
    public void clear() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        Assertions.assertNotNull(db.getUser("james"));

        userService.clear();

        Assertions.assertNull(db.getUser("james"));

    }
}
