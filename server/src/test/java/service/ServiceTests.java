package service;

import dataaccess.DataAccessException;
import dataaccess.SQLDataAccess;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;


public class ServiceTests {
    private static SQLDataAccess db;
    private static UserService userService;
    private static GameService gameService;
    private static UserData newUser;
    private static GameData newGame;

    @BeforeAll
    public static void init() throws DataAccessException {
        db = new SQLDataAccess();
        userService = new UserService(db);
        gameService = new GameService(db);
        newUser = new UserData("james", "12345", "james@mynameisjames.com");
    }

    @BeforeEach
    public void reset() throws DataAccessException {
        userService.clear();
        userService.register(new RegisterRequest("DefaultUser", "12345", "james@mynameisjames.com"));
    }
    //list of all public methods
    //register
    @Test
    public void registerSuccess() throws DataAccessException {
        var registerRes = userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        Assertions.assertEquals(newUser.username(), db.getUser("james").username());
        Assertions.assertTrue(BCrypt.checkpw(newUser.password(), db.getUser("james").password()));
        Assertions.assertEquals(newUser.email(), db.getUser("james").email());
        Assertions.assertNotNull(db.getAuth(registerRes.authToken()));
    }
    @Test
    public void registerAlreadyExists() throws DataAccessException {
        var registerRes = userService.register(new RegisterRequest("james", "12345", "jamesjohnson@mynameisjames.com"));
        var registerResAgain = userService.register(new RegisterRequest("james", "abcde", "jamesfrederickson@jamesisthebest.com"));
        Assertions.assertNotNull(registerResAgain.message());
    }
    @Test
    public void registerInvalidInput() throws DataAccessException {
        var registerRes = userService.register(new RegisterRequest("james", null, "jamesjohnson@mynameisjames.com"));
        Assertions.assertNotNull(registerRes.message());
    }
    //login
    @Test
    public void loginSuccess() throws DataAccessException {

        LoginRequest loginRequest = new LoginRequest("DefaultUser", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        Assertions.assertNull(loginResponse.message());
    }
    @Test
    public void loginDoesntExist() throws DataAccessException {

        LoginRequest loginRequest = new LoginRequest("patrick", "nothisispatrick");
        LoginResponse loginResponse = userService.login(loginRequest);
        Assertions.assertNotNull(loginResponse.message());
        Assertions.assertNull(loginResponse.authToken());
    }

    @Test
    public void loginWrongPassword() throws DataAccessException {

        LoginRequest loginRequest = new LoginRequest("james", "01234");
        LoginResponse loginResponse = userService.login(loginRequest);
        Assertions.assertNotNull(loginResponse.message());
    }

    //logout
    @Test
    public void logoutSuccess() throws DataAccessException {

        LoginRequest loginRequest = new LoginRequest("DefaultUser", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);

        LogoutRequest logoutRequest = new LogoutRequest(loginResponse.authToken());
        LogoutResponse logoutResponse = userService.logout(logoutRequest);
        Assertions.assertNull(logoutResponse.message());
        Assertions.assertNull(db.getAuth(loginResponse.authToken()));
    }

    @Test
    public void logoutUnauthorized() throws DataAccessException {
        //user attempts to log out but doesn't have auth token
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);

        LogoutRequest logoutRequest = new LogoutRequest(loginResponse.authToken());
        LogoutResponse logoutResponse = userService.logout(logoutRequest);

        LogoutRequest logoutRequestAgain = new LogoutRequest(loginResponse.authToken());
        LogoutResponse logoutResponseAgain = userService.logout(logoutRequestAgain);
        Assertions.assertNotNull(logoutResponseAgain.message());
    }
    //createGame
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
    public void createUnauthorized() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);

        LogoutRequest logoutRequest = new LogoutRequest(loginResponse.authToken());
        LogoutResponse logoutResponse = userService.logout(logoutRequest);

        CreateResponse createResponse = gameService.createGame(new CreateRequest(loginResponse.authToken(), "myGame"));
        Assertions.assertNotNull(createResponse.message());
    }

    @Test
    public void createBadInput() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);

        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), null);
        CreateResponse createResponse = gameService.createGame(createRequest);
        Assertions.assertNull(createResponse.gameID());
        Assertions.assertNotNull(createResponse.message());
    }
    //joinGame
    @Test
    public void joinSuccess() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        LoginRequest loginRequest2 = new LoginRequest("DefaultUser", "12345");
        LoginResponse loginResponse2 = userService.login(loginRequest2);
        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), "myGame");
        CreateResponse createResponse = gameService.createGame(createRequest);
        String gameID = createResponse.gameID();
        JoinRequest joinRequest = new JoinRequest(loginResponse.authToken(), "WHITE", gameID);
        JoinResponse joinResponse = gameService.joinGame(joinRequest);
        JoinRequest joinRequest2 = new JoinRequest(loginResponse2.authToken(), "BLACK", gameID);
        JoinResponse joinResponse2 = gameService.joinGame(joinRequest2);
        Assertions.assertNull(joinResponse.message());
        Assertions.assertNull(joinResponse2.message());
    }

    @Test
    public void joinUnauthorized() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), "myGame");
        CreateResponse createResponse = gameService.createGame(createRequest);
        String gameID = createResponse.gameID();
        LogoutResponse logoutResponse = userService.logout(new LogoutRequest(loginResponse.authToken()));

        JoinRequest joinRequest = new JoinRequest(loginResponse.authToken(), "WHITE", gameID);
        JoinResponse joinResponse = gameService.joinGame(joinRequest);
        Assertions.assertNotNull(joinResponse.message());
    }

    @Test
    public void joinBadRequest() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), "myGame");
        CreateResponse createResponse = gameService.createGame(createRequest);
        String gameID = createResponse.gameID();
        LogoutResponse logoutResponse = userService.logout(new LogoutRequest(loginResponse.authToken()));

        JoinRequest joinRequest = new JoinRequest(loginResponse.authToken(), "WHIT", gameID);
        JoinResponse joinResponse = gameService.joinGame(joinRequest);
        Assertions.assertNotNull(joinResponse.message());
    }

    @Test
    public void joinAlreadyTaken() throws DataAccessException {
        userService.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        userService.register(new RegisterRequest("john", "123456", "john@mynameisjohn.com"));
        LoginRequest loginRequest = new LoginRequest("james", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        LoginResponse loginResponse2 = userService.login(new LoginRequest("john", "123456"));
        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), "myGame");
        CreateResponse createResponse = gameService.createGame(createRequest);
        String gameID = createResponse.gameID();


        JoinRequest joinRequest = new JoinRequest(loginResponse.authToken(), "WHITE", gameID);
        JoinRequest joinRequest2 = new JoinRequest(loginResponse2.authToken(), "WHITE", gameID);
        JoinResponse joinResponse = gameService.joinGame(joinRequest);
        JoinResponse joinResponse2 = gameService.joinGame(joinRequest2);
        Assertions.assertNotNull(joinResponse2.message());
    }
    //listGames
    @Test
    public void listSuccess() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("DefaultUser", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), "myGame");
        CreateResponse createResponse = gameService.createGame(createRequest);

        ListRequest listRequest = new ListRequest(loginResponse.authToken());
        ListResponse listResponse = gameService.listGames(listRequest);
        Assertions.assertNull(listResponse.message());
    }
    @Test
    public void listUnauthorized() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("DefaultUser", "12345");
        LoginResponse loginResponse = userService.login(loginRequest);
        CreateRequest createRequest = new CreateRequest(loginResponse.authToken(), "myGame");
        gameService.createGame(createRequest);
        userService.logout(new LogoutRequest(loginResponse.authToken()));

        ListRequest listRequest = new ListRequest(loginResponse.authToken());
        ListResponse listResponse = gameService.listGames(listRequest);
        Assertions.assertNotNull(listResponse.message());
    }
    //clear
    @Test
    public void clear() throws DataAccessException {
        Assertions.assertNotNull(db.getUser("DefaultUser"));

        userService.clear();

        Assertions.assertNull(db.getUser("DefaultUser"));

    }
}
