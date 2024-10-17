package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    AuthData register(UserData user) throws DataAccessException;
}
