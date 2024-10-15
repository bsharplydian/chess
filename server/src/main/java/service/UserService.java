package service;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;

public class UserService {
    public String register(UserData user) {
        return """
                {"service register":"passed"}""";
    }

}
