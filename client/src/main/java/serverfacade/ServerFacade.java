package serverfacade;

import com.google.gson.Gson;
import response.*;
import request.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.Objects;

public class ServerFacade {
    private final String serverUrl;
    public ServerFacade(String url) {
        serverUrl = url;
    }
    public void clear() throws Exception {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }
    public RegisterResponse addUser(RegisterRequest registerRequest) throws Exception {
        var path = "/user";
        RegisterResponse response = this.makeRequest("POST", path, registerRequest, RegisterResponse.class, null);
        if(response.message() != null) {
            throw new Exception(response.message());
        }
        return response;
    }

    public LoginResponse login(LoginRequest loginRequest) throws Exception {
        var path = "/session";
        LoginResponse response = this.makeRequest("POST", path, loginRequest, LoginResponse.class, null);
        if(response.message() != null) {
            throw new Exception(response.message());
        }
        return response;
    }

    public LogoutResponse logout(LogoutRequest logoutRequest) throws Exception {
        var path = "/session";
        LogoutResponse response = this.makeRequest("DELETE", path, logoutRequest, LogoutResponse.class, logoutRequest.authToken());
        if(response.message() != null) {
            throw new Exception(response.message());
        }
        return response;
    }

    public CreateResponse createGame(CreateRequest createRequest) throws Exception {
        var path = "/game";
        CreateResponse response = this.makeRequest("POST", path, createRequest, CreateResponse.class, createRequest.authToken());
        if(response.message() != null) {
            throw new Exception(response.message());
        }
        return response;
    }

    public ListResponse listGames(ListRequest listRequest) throws Exception {
        var path = "/game";
        ListResponse response = this.makeRequest("GET", path, listRequest, ListResponse.class, listRequest.authToken());
        if(response.message() != null) {
            throw new Exception(response.message());
        }
        return response;
    }

    public JoinResponse joinAsColor(JoinRequest joinRequest) throws Exception {
        var path = "/game";
        JoinResponse response = this.makeRequest("PUT", path, joinRequest, JoinResponse.class, joinRequest.authToken());
        if(response.message() != null) {
            throw new Exception(response.message());
        }
        return response;
    }

    public String JoinAsObserver() throws Exception {
        return "not implemented";
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
            if(!Objects.equals(method, "GET")) {
                writeBody(request, http);
            }
            http.connect();
            //throwIfNotSuccessful(http);
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
            catch (IOException ex) {
                try(InputStream respBody = http.getErrorStream()) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    if(responseClass != null) {
                        response = new Gson().fromJson(reader, responseClass);
                    }
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
