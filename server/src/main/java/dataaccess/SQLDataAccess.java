package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.ArrayList;

public class SQLDataAccess implements DataAccess{

    SQLDataAccess () {

    }
    @Override
    public void createUser(UserData userData) {

    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public UserData getUserByAuth(String authToken) {
        return null;
    }

    @Override
    public void createAuth(AuthData auth) {

    }

    @Override
    public AuthData getAuth(String authToken) {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public void deleteAuth(String s) {

    }

    @Override
    public int createGame(String s) {
        return 0;
    }

    @Override
    public GameData getGame(int gameID) {
        return null;
    }

    @Override
    public void updateGame(int gameID, GameData gameData) {

    }

    @Override
    public ArrayList<GameData> listGames() {
        return null;
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  users (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(username),
              INDEX(email)
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
}
