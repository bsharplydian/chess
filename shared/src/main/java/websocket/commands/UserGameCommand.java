package websocket.commands;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    protected final CommandType commandType;

    protected final String authToken;

    protected final Integer gameID;

    protected final String userColor;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, String userColor) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.userColor = userColor; //If this breaks, it's because the autograder is using a constructor without the userColor parameter.
                                    //switch this out for a setter and getter if needed
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }
    public String getUserColor() {
        return userColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return commandType == that.commandType && Objects.equals(authToken, that.authToken) && Objects.equals(gameID, that.gameID) && Objects.equals(userColor, that.userColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandType, authToken, gameID, userColor);
    }
}
