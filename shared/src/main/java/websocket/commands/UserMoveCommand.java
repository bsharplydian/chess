package websocket.commands;

import chess.ChessMove;

import java.util.Objects;

public class UserMoveCommand extends UserGameCommand{
    private final ChessMove move;
    public UserMoveCommand(CommandType commandType, String authToken, Integer gameID, String userColor, ChessMove move) {
        super(commandType, authToken, gameID, userColor);
        this.move = move;
    }
    public ChessMove getMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserMoveCommand that = (UserMoveCommand) o;
        return commandType == that.commandType && Objects.equals(authToken, that.authToken) && Objects.equals(gameID, that.gameID)
                && Objects.equals(userColor, that.userColor) && Objects.equals(move, that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandType, authToken, gameID, userColor, move);
    }
}
