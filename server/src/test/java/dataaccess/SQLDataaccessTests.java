package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

public class SQLDataaccessTests {
    private static DataAccess dataAccess;
    @BeforeAll
    public static void init() throws DataAccessException {
        dataAccess = new SQLDataAccess();
    }
    @Test
    public void registerSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("jeff", "password", "jeff@james.com"));
        Assertions.assertEquals(dataAccess.getUser("jeff").getClass(), UserData.class);
    }
}
