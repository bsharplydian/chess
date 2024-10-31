package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLDataAccess implements DataAccess{

    public SQLDataAccess () throws DataAccessException {
        configureDatabase();
    }
    @Override
    public void createUser(UserData userData) throws DataAccessException{
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        executeUpdate(statement, userData.username(), userData.password(), userData.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException{
        var statement = "SELECT username, password, email FROM users WHERE username=?";
        try(var conn = DatabaseManager.getConnection()) {
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }
        return null;
    }
    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString(1);
        var password = rs.getString(2);
        var email = rs.getString(3);
        return new UserData(username, password, email);
    }

    @Override
    public UserData getUserByAuth(String authToken) throws DataAccessException{
        var statement = "SELECT users.username, password, email FROM users JOIN authtokens WHERE authtoken=?";
        try(var conn = DatabaseManager.getConnection()) {
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }
        return null;
    }


    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        var statement = "INSERT INTO authtokens (username, authtoken) VALUES (?, ?)";
        executeUpdate(statement, auth.username(), auth.authToken());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT username, authtoken FROM authtokens WHERE authtoken=?";
        try(var conn = DatabaseManager.getConnection()) {
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }
        return null;
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var username = rs.getString(1);
        var authtoken = rs.getString(2);
        return new AuthData(username, authtoken);
    }
    @Override
    public void clear() throws DataAccessException {
        executeUpdate("TRUNCATE TABLE users");
        executeUpdate("TRUNCATE TABLE authtokens");
        executeUpdate("TRUNCATE TABLE games");
    }

    @Override
    public void deleteAuth(String s) throws DataAccessException {
        if(s == null) {
            throw new DataAccessException("no authtoken given");
        }
        var statement = "DELETE FROM authtokens WHERE authtoken = ?";
        executeUpdate(statement, s);
    }

    @Override
    public int createGame(String s) throws DataAccessException {
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, gameJson) VALUES (?, ?, ?, ?)";
        ChessGame game = new ChessGame();

        return executeUpdate(statement, null, null, s, new Gson().toJson(game));
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT whiteUsername, blackUsername, gameName, gameJson FROM games WHERE id=?";
        try(var conn = DatabaseManager.getConnection()) {
            try(var ps = conn.prepareStatement(statement)) {
                ps.setString(1, String.valueOf(gameID));
                try (var rs = ps.executeQuery()) {
                    if(rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }
        return null;
    }

    private GameData readGame(ResultSet rs) throws SQLException{
        var id = rs.getRow();
        var whiteUsername = rs.getString(1);
        var blackUsername = rs.getString(2);
        var gameName = rs.getString(3);
        var gameJson = rs.getString(4);
        ChessGame chessGame = new Gson().fromJson(gameJson, ChessGame.class);
        return new GameData(id, whiteUsername, blackUsername, gameName, chessGame);
    }

    @Override
    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        var gameJson = new Gson().toJson(gameData.game());
        var statement = "UPDATE games SET whiteUsername=?, blackUsername=?, gameName=?, gameJson=? WHERE id=?";

        executeUpdate(statement, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameJson, gameID);
    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException{
        ArrayList<GameData> games = new ArrayList<>();
        var statement = "SELECT whiteUsername, blackUsername, gameName, gameJson FROM games";
        try(var conn = DatabaseManager.getConnection()) {
            try(var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        games.addLast(readGame(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }
        return games;
    }

    private ArrayList<Integer> getGameIDs() throws DataAccessException{
        ArrayList<Integer> gameIDs = new ArrayList<>();
        var statement = "SELECT id FROM games";
        try(var conn = DatabaseManager.getConnection()) {
            try(var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        gameIDs.addLast(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }

        return gameIDs;
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  users (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NOT NULL UNIQUE,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`id`),
              INDEX(username),
              INDEX(email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS  authtokens (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NOT NULL UNIQUE,
              `authtoken` varchar(256) NOT NULL,
              PRIMARY KEY (`id`),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS  games (
              `id` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256),
              `blackUsername` varchar(256),
              `gameName` varchar(256) NOT NULL,
              `gameJson` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            // modify return value of object to contain the given message
        }
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            try(var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for(var i = 0; i < params.length; i++){
                    var param = params[i];
                    if(param instanceof String p) {
                        ps.setString(i+1, p);
                    } else if(param instanceof Integer p) {
                        ps.setInt(i+1, p);
                    } else if (param == null) {
                        ps.setNull(i+1, NULL);
                    }
                }
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                if(rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }

    }
}
