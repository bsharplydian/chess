package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import request.LoginRequest;
import request.LogoutRequest;
import request.RegisterRequest;
import response.LoginResponse;
import response.LogoutResponse;


public class ServiceTests {
    private static MemoryDataAccess db;
    private static UserService userService;
    private static GameService gameService;
    private static UserData newUser;

    @BeforeAll
    public static void init() {
        db = new MemoryDataAccess();
        userService = new UserService(db);
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
    public void LoginFailDoesntExist() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        LoginRequest loginRequest = new LoginRequest("patrick", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        Assertions.assertNotNull(loginResponse.message());
    }

    @Test
    public void LoginFailWrongPassword() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        LoginRequest loginRequest = new LoginRequest("james", "01234");
        LoginResponse loginResponse = userService.login(loginRequest);
        Assertions.assertNotNull(loginResponse.message());
    }

    @Test
    public void LogoutSuccess() throws DataAccessException {
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
    public void LogoutFail() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);

        LogoutRequest logoutRequest = new LogoutRequest(loginResponse.authToken());
        LogoutResponse logoutResponse = userService.logout(logoutRequest);
        Assertions.assertNotNull(logoutResponse.message());
        Assertions.assertNotNull(db.getAuth(loginResponse.authToken()));
    }

    @Test
    public void clear() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        Assertions.assertNotNull(db.getUser("james"));

        userService.clear();

        Assertions.assertNull(db.getUser("james"));

    }
}
