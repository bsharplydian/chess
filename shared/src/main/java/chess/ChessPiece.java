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
        while(row + distance <= 8 && row - distance > 0 && column + distance <= 8 && column - distance > 0) {
            ChessPosition checkPos = switch (direction) {
                case "ne" -> new ChessPosition(row + distance, column + distance);
                case "se" -> new ChessPosition(row - distance, column + distance);
                case "sw" -> new ChessPosition(row - distance, column - distance);
                case "nw" -> new ChessPosition(row + distance, column - distance);
                case "n" -> new ChessPosition(row + distance, column);
                case "e" -> new ChessPosition(row, column + distance);
                case "s" -> new ChessPosition(row-distance, column);
                case "w" -> new ChessPosition(row, column-distance);
                default -> new ChessPosition(1, 1);
            };

            ChessMove potentialMove = new ChessMove(position, checkPos, null);
            if(board.getPiece(checkPos) == null || board.getPiece(checkPos).getTeamColor() != thisPiece.getTeamColor()){
                //if the selected square is empty or has a piece from the opposite team, add the move to the move list
                moves.add(potentialMove);
                distance += 1;
            }
            else
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
