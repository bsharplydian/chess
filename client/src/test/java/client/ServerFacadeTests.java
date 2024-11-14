package client;

import org.junit.jupiter.api.*;
import response.*;
import request.*;
import server.Server;
import serverfacade.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        serverFacade.clear();
    }

    @AfterAll
    static void stopServer() throws Exception {
        serverFacade.clear();
        server.stop();
    }

    @Test
    public void registerSuccessful() throws Exception {
        RegisterResponse registerResponse = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        Assertions.assertEquals("a", registerResponse.username());
        Assertions.assertEquals(36, registerResponse.authToken().length());
        Assertions.assertNull(registerResponse.message());
    }

    @Test
    public void registerDuplicate() throws Exception {
        RegisterResponse registerResponse = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        Assertions.assertThrows(Exception.class,
                () -> serverFacade.addUser(new RegisterRequest("a", "extrasecret", "a@a.com")));
    }

    @Test
    public void loginSuccess() throws Exception {
        RegisterResponse registerResponse = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        serverFacade.logout(new LogoutRequest(registerResponse.authToken()));
        Assertions.assertDoesNotThrow(() ->serverFacade.login(new LoginRequest("a", "secret")));
    }

    @Test
    public void loginWrongPassword() throws Exception {
        RegisterResponse registerResponse = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        serverFacade.logout(new LogoutRequest(registerResponse.authToken()));
        Assertions.assertThrows(Exception.class, () ->serverFacade.login(new LoginRequest("a", "wrongPass")));
    }

    @Test
    public void logoutSuccess() throws Exception {
        RegisterResponse registerResponse = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(new LogoutRequest(registerResponse.authToken())));
    }

    @Test
    public void logoutNoAuth() {
        Assertions.assertThrows(Exception.class, () -> serverFacade.logout(new LogoutRequest(null)));
    }

    @Test
    public void clear() throws Exception {
        RegisterResponse registerResponse = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        CreateResponse createResponse = serverFacade.createGame(new CreateRequest(registerResponse.authToken(), "testGame1"));
        serverFacade.clear();
        Assertions.assertThrows(Exception.class, () -> serverFacade.login(new LoginRequest("a", "secret")));
    }

    @Test
    public void createSuccess() throws Exception {
        RegisterResponse registerResponse = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        Assertions.assertDoesNotThrow(() ->
                serverFacade.createGame(new CreateRequest(registerResponse.authToken(), "testGame2")));
    }

    @Test
    public void createUnauthorized() {
        Assertions.assertThrows(Exception.class, () ->
                serverFacade.createGame(new CreateRequest(null, "testGame3")));
    }

    @Test
    public void listSuccess() throws Exception {
        RegisterResponse registerResponse = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        CreateResponse createResponse1 = serverFacade.createGame(new CreateRequest(registerResponse.authToken(), "testGame1"));
        CreateResponse createResponse2 = serverFacade.createGame(new CreateRequest(registerResponse.authToken(), "testGame2"));
        CreateResponse createResponse3 = serverFacade.createGame(new CreateRequest(registerResponse.authToken(), "testGame3"));
        ListRequest listRequest = new ListRequest(registerResponse.authToken());
        ListResponse listResponse = serverFacade.listGames(listRequest);
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.listGames(listRequest);
        });
        Assertions.assertEquals("testGame1", listResponse.games().getFirst().gameName());
        Assertions.assertEquals("testGame2", listResponse.games().get(1).gameName());
        Assertions.assertEquals("testGame3", listResponse.games().get(2).gameName());
    }

    @Test
    public void listUnauthorized() {
        Assertions.assertThrows(Exception.class, () -> serverFacade.listGames(new ListRequest(null)));
    }

    @Test
    public void joinSuccess() throws Exception {
        RegisterResponse registerResponse = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        CreateResponse createResponse1 = serverFacade.createGame(new CreateRequest(registerResponse.authToken(), "testGame1"));

        Assertions.assertDoesNotThrow(() ->
                serverFacade.joinGame(new JoinRequest(registerResponse.authToken(), "WHITE", "1")));
    }

    @Test
    public void joinAlreadyTaken() throws Exception {
        RegisterResponse registerResponse1 = serverFacade.addUser(new RegisterRequest("a", "secret", "a@a.com"));
        RegisterResponse registerResponse2 = serverFacade.addUser(new RegisterRequest("b", "secret2", "b@b.com"));
        CreateResponse createResponse1 = serverFacade.createGame(new CreateRequest(registerResponse1.authToken(), "testGame1"));
        serverFacade.joinGame(new JoinRequest(registerResponse2.authToken(), "WHITE", "1"));
        Assertions.assertThrows(Exception.class, () ->
                serverFacade.joinGame(new JoinRequest(registerResponse1.authToken(), "WHITE", "1")));
    }

}
