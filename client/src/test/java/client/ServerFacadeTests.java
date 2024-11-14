package client;

import org.junit.jupiter.api.*;
import request.*;
import response.*;
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
    static void stopServer() {
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
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
