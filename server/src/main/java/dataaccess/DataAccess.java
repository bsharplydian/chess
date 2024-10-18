package dataaccess;

import model.UserData;

public interface DataAccess {
//    AuthData register(UserData user);
    void createUser(UserData userData);
    UserData getUser(String username);
}
