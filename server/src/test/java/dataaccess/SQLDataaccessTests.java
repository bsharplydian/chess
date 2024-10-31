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
    public void addSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("jeff", "password", "jeff@james.com"));
        Assertions.assertEquals(dataAccess.getUser("jeff").getClass(), UserData.class);
    }

    @Test
    public void addFailure() throws DataAccessException {

    }

    @Test
    public void getSuccess() throws DataAccessException {
        UserData jamesData = new UserData("james", "pass", "james@james.com");
        dataAccess.createUser(jamesData);
        Assertions.assertEquals(jamesData, dataAccess.getUser("james"));
    }

    @Test
    public void getFailure() throws DataAccessException {

    }
}
