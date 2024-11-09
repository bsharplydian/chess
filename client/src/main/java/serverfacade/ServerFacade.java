package serverfacade;

import com.google.gson.Gson;
import model.UserData;

public class ServerFacade {
    private final String serverUrl;
    public ServerFacade(String url) {
        serverUrl = url;
    }
    public UserData addUser(UserData userData) {
        var path = "/user";
        return this.makeRequest("POST", path, userData, UserData.class);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) {
        String json = new Gson().toJson(new UserData("james", "jamismyjam", "james@phrederick.com"));
        return new Gson().fromJson(json, responseClass);
    }

}
