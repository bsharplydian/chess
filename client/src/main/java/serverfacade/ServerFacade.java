package serverfacade;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import request.*;
import response.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

public class ServerFacade {
    private final String serverUrl;
    public ServerFacade(String url) {
        serverUrl = url;
    }
    public RegisterResponse addUser(RegisterRequest registerRequest) throws Exception {
        var path = "/user";
        return this.makeRequest("POST", path, registerRequest, RegisterResponse.class, null);
    }

    public LoginResponse login(LoginRequest loginRequest) throws Exception {
        var path = "/session";
        return this.makeRequest("POST", path, loginRequest, LoginResponse.class, null);
    }

    public LogoutResponse logout(LogoutRequest logoutRequest) throws Exception {
        var path = "/session";
        return this.makeRequest("DELETE", path, logoutRequest, LogoutResponse.class, logoutRequest.authToken());
    }

    public CreateResponse createGame(CreateRequest createRequest) throws Exception {
        var path = "/game";
        return this.makeRequest("POST", path, createRequest, CreateResponse.class, createRequest.authToken());
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if(authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }
            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);

        } catch (IOException ex) {
            //do something here probably
            throw new Exception(ex.getMessage());
        }

    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException {
        var status = http.getResponseCode();
        if (!(status / 100 == 2)) {
            throw new IOException("request not successful");
        }
    }

}
