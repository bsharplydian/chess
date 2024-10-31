package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.SQLException;

public class SQLDataaccessTests {
    private static DataAccess dataAccess;
    @BeforeAll
    public static void init() throws DataAccessException {
        dataAccess = new SQLDataAccess();
        dataAccess.clear();
    }

    @BeforeEach
    public void beforeEach() throws DataAccessException {
        //dataAccess.clear();
    }
    @Test
    public void addUserSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("jeff", "password", "jeff@james.com"));

        Assertions.assertEquals(dataAccess.getUser("jeff").getClass(), UserData.class);
    }

    @Test
    public void addUserAlreadyExists() throws DataAccessException {
        dataAccess.createUser(new UserData("jeff", "password", "jeff@james.com"));
        Assertions.assertThrows(DataAccessException.class, () -> {
            dataAccess.createUser(new UserData("jeff", "password", "jeff@james.com"));
        });
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
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
    public void getGameSuccess() throws DataAccessException {
        int id = dataAccess.createGame("myGame1");
        Assertions.assertEquals("myGame1", dataAccess.getGame(id).gameName());

    }

    @Test
    public void getGameDoesntExist() throws DataAccessException {
        Assertions.assertNull(dataAccess.getGame(4204039));
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        int id = dataAccess.createGame("myGame");
        Assertions.assertEquals("myGame", dataAccess.getGame(id).gameName());
        int id3 = dataAccess.createGame("game3");
        int id2 = dataAccess.createGame("game2");
        Assertions.assertEquals("game3", dataAccess.getGame(id3).gameName());
        Assertions.assertEquals("game2", dataAccess.getGame(id2).gameName());
    }

    @Test
    public void createGameFailure() throws DataAccessException {

    }

    @Test
    public void updateGameSuccess() throws DataAccessException {
        int id = dataAccess.createGame("myGame");
        GameData newData = new GameData(id, "james", "john", "myGame", new ChessGame());
        dataAccess.updateGame(id, newData);
        Assertions.assertEquals("james", dataAccess.getGame(id).whiteUsername());
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("shortlived", "pass", "donteraseme@.com"));
        dataAccess.clear();
        Assertions.assertNull(dataAccess.getUser("shortlived"));
    }
}
