package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
abstract class MoveCalculator {

    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

    }
}
class BishopMoveCalculator extends MoveCalculator {

}

public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> checkLaser(int row, int column, ChessBoard board, ChessPosition position, ChessPiece thisPiece, String direction) {
        int distance = 1;
        Collection<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPosition checkPos;

        while(true) {
            switch (direction) {
                case "ne":
                    checkPos = new ChessPosition(row + distance, column + distance);
                    break;
                case "se":
                    checkPos = new ChessPosition(row - distance, column + distance);
                    break;
                case "sw":
                    checkPos = new ChessPosition(row - distance, column - distance);
                    break;
                case "nw":
                    checkPos = new ChessPosition(row + distance, column - distance);
                    break;
                case "n":
                    checkPos = new ChessPosition(row + distance, column);
                    break;
                case "e":
                    checkPos = new ChessPosition(row, column + distance);
                    break;
                case "s":
                    checkPos = new ChessPosition(row - distance, column);
                    break;
                case "w":
                    checkPos = new ChessPosition(row, column - distance);
                    break;
                default:
                    checkPos = new ChessPosition(1, 1);
                    break;
            }
            if(checkPos.getColumn() > 8 || checkPos.getColumn() < 1 || checkPos.getRow() > 8 || checkPos.getRow() < 1)
                break; // ensures that the checked position is within the bounds of the board

            ChessMove potentialMove = new ChessMove(position, checkPos, null);
            if(board.getPiece(checkPos) == null){
                //if the selected square is empty, add the move to the move list and continue counting
                moves.add(potentialMove);
                distance += 1;
            } else if (board.getPiece(checkPos).getTeamColor() != thisPiece.getTeamColor()){
                //if it has an opposite team piece, add the move and stop counting
                moves.add(potentialMove);
                break;
            }
            else //if there is a piece of your own team, don't add the move, and stop counting
                break;
        }

        return moves;
    }
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece thisPiece = board.getPiece(myPosition); //stores current piece object in a variable
        Collection<ChessMove> moves = new HashSet<ChessMove>();

        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();

        switch(thisPiece.getPieceType()) {
            case BISHOP:
                //checks a laser in each direction and adds the available spaces in each direction to moves
                moves.addAll(checkLaser(myRow, myCol, board, myPosition, thisPiece,"ne"));
                moves.addAll(checkLaser(myRow, myCol, board, myPosition, thisPiece,"se"));
                moves.addAll(checkLaser(myRow, myCol, board, myPosition, thisPiece,"sw"));
                moves.addAll(checkLaser(myRow, myCol, board, myPosition, thisPiece,"nw"));
        }
        return moves;
    }
}
