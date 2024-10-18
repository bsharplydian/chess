package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;


public class ServiceTests {

    @Test
    public void register() {
        MemoryDataAccess db = new MemoryDataAccess();
        UserService service = new UserService(db);
        UserData newUser = new UserData("james", "12345", "james@mynameisjames.com");
        service.register(newUser);

        Assertions.assertEquals(newUser, db.getUser("james"));
    }
    public void getAuth() {

    }

}
