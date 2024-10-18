package service;

import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import request.RegisterRequest;


public class ServiceTests {

    @Test
    public void register() {
        MemoryDataAccess db = new MemoryDataAccess();
        UserService service = new UserService(db);
        UserData newUser = new UserData("james", "12345", "james@mynameisjames.com");
        var registerRes = service.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));

        Assertions.assertEquals(newUser, db.getUser("james"));
        Assertions.assertNotNull(db.getAuth(registerRes.authToken()));
    }
    @Test
    public void registerAndCheckAuth() {
        MemoryDataAccess db = new MemoryDataAccess();
        UserService service = new UserService(db);
        var registerResult = service.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        AuthData authData = new AuthData(registerResult.username(), registerResult.authToken());
        Assertions.assertEquals(36, authData.authToken().length());
        Assertions.assertEquals("james", authData.username());

        //this test doesn't do anything
    }


    @Test
    public void clear() {
        MemoryDataAccess db = new MemoryDataAccess();
        UserService service = new UserService(db);
        service.register(new RegisterRequest("james", "12345", "james@mynameisjames.com"));
        Assertions.assertNotNull(db.getUser("james"));

        service.clear();

        Assertions.assertNull(db.getUser("james"));

    }
}
