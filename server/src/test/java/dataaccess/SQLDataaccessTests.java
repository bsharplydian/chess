package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class SQLDataaccessTests {
    private static DataAccess dataAccess;
    @BeforeAll
    public static void init() throws DataAccessException {
        dataAccess = new SQLDataAccess();
        dataAccess.clear();
    }
    @Test
    public void addSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("jeff", "password", "jeff@james.com"));

        Assertions.assertEquals(dataAccess.getUser("jeff").getClass(), UserData.class);
    }

    @Test
    public void addAlreadyExists() throws DataAccessException {
        dataAccess.createUser(new UserData("jeff", "password", "jeff@james.com"));
        Assertions.assertThrows(DataAccessException.class, () -> {
            dataAccess.createUser(new UserData("jeff", "password", "jeff@james.com"));
        });
    }

    @Test
    public void getSuccess() throws DataAccessException {
        UserData jamesData = new UserData("james", "pass", "james@james.com");
        dataAccess.createUser(jamesData);
        Assertions.assertEquals(jamesData, dataAccess.getUser("james"));
    }

    @Test
    public void getUserDoesNotExist() throws DataAccessException {
        Assertions.assertNull(dataAccess.getUser("jimothy"));
    }

    @Test
    public void getUserByAuthSuccess() throws DataAccessException {
        UserData jamesData = new UserData("james", "pass", "james@james.com");
        AuthData jamesAuth = new AuthData("james", "notASecureToken");
        dataAccess.createUser(jamesData);
        dataAccess.createAuth(jamesAuth);
        Assertions.assertEquals(jamesData, dataAccess.getUserByAuth("notASecureToken"));
    }

    @Test
    public void getUserByAuthDoesNotExist() throws DataAccessException {
        Assertions.assertNull(dataAccess.getUserByAuth("tokenDoesntExist"));
    }

    @Test
    public void getAuthSuccess() throws DataAccessException {
        AuthData jamesAuth = new AuthData("james", "notASecureToken");
        dataAccess.createAuth(jamesAuth);
        Assertions.assertEquals(jamesAuth, dataAccess.getAuth("notASecureToken"));
    }

    @Test
    public void getAuthFail() throws DataAccessException {
        Assertions.assertNull(dataAccess.getAuth("thisAuthDoesntExist"));
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        AuthData jamesAuth = new AuthData("james", "totallySecureToken");
        dataAccess.createAuth(jamesAuth);
        dataAccess.deleteAuth("totallySecureToken");
        Assertions.assertNull(dataAccess.getAuth("totallySecureToken"));
    }

    @Test
    public void deleteAuthFailure() throws DataAccessException {
        Assertions.assertThrows(DataAccessException.class, () -> {
            dataAccess.deleteAuth(null);
        }
        );
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("shortlived", "pass", "donteraseme@.com"));
        dataAccess.clear();
        Assertions.assertNull(dataAccess.getUser("shortlived"));
    }
}
