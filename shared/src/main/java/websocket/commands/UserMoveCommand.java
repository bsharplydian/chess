package websocket.commands;

import chess.ChessMove;

import java.util.Objects;

public class UserMoveCommand extends UserGameCommand{
    private final ChessMove chessMove;
    public UserMoveCommand(CommandType commandType, String authToken, Integer gameID, String userColor, ChessMove chessMove) {
        super(commandType, authToken, gameID, userColor);
        this.chessMove = chessMove;
    }
    public ChessMove getMove() {
        return chessMove;
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
                && Objects.equals(userColor, that.userColor) && Objects.equals(chessMove, that.chessMove);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandType, authToken, gameID, userColor, chessMove);
    }
}
