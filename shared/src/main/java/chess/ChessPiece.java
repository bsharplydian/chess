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

    private final ChessBoard board;
    private final ChessPosition myPosition;

    public MoveCalculator(ChessBoard board, ChessPosition myPosition) {
        this.board = board;
        this.myPosition = myPosition;
    }
//    Collection<ChessMove> calculateMoves() {
//
//    }
    Collection<ChessMove> checkLaser(int row, int column, int range, ChessBoard board, ChessPosition position, ChessPiece thisPiece, String direction) {
        int distance = 1;
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPosition checkPos;
        int counter = range;
        while(true) {
            checkPos = switch (direction) {
                case "ne" -> new ChessPosition(row + distance, column + distance);
                case "se" -> new ChessPosition(row - distance, column + distance);
                case "sw" -> new ChessPosition(row - distance, column - distance);
                case "nw" -> new ChessPosition(row + distance, column - distance);
                case "n" -> new ChessPosition(row + distance, column);
                case "e" -> new ChessPosition(row, column + distance);
                case "s" -> new ChessPosition(row - distance, column);
                case "w" -> new ChessPosition(row, column - distance);
                default -> new ChessPosition(1, 1);
            };
            if(checkPos.getColumn() > 8 || checkPos.getColumn() < 1 || checkPos.getRow() > 8 || checkPos.getRow() < 1 || counter == 0)
                break; // ensures that the checked position is within the bounds of the board and that it is within the range of the piece (either 1 or infinity)

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
            counter--;
        }

        return moves;
    }
    Collection<ChessMove> checkSpace(int row, int column, ChessBoard board, ChessPosition position, ChessPiece thisPiece, int rowDiff, int colDiff) {
        Collection<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPosition checkPos = new ChessPosition(row + rowDiff, column + colDiff);
        ChessMove potentialMove = new ChessMove(position, checkPos, null);

        if(checkPos.getColumn() <= 8 || checkPos.getColumn() >= 1 || checkPos.getRow() <= 8 || checkPos.getRow() >= 1) {
            if(thisPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if(colDiff != 0 && board.getPiece(checkPos).getTeamColor() != thisPiece.getTeamColor()) //capture diagonals
                    moves.add(potentialMove);
                else if(colDiff == 0 && board.getPiece(checkPos) == null) //move forward
                    moves.add(potentialMove);
            }
        }
        return moves;
    }
}

class BishopMoveCalculator extends MoveCalculator {
    private final ChessBoard board;
    private final ChessPosition myPosition;
    public BishopMoveCalculator(ChessBoard board, ChessPosition myPosition) {
        super(board, myPosition);
        this.board = board;
        this.myPosition = myPosition;
    }

    Collection<ChessMove> calculateMoves() {
        ChessPiece thisPiece = board.getPiece(myPosition); //stores current piece object in a variable
        Collection<ChessMove> moves = new HashSet<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        //checks a laser in each direction and adds the available spaces in each direction to moves
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"ne"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"se"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"sw"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"nw"));
        return moves;
    }
}

class RookMoveCalculator extends MoveCalculator {
    private final ChessBoard board;
    private final ChessPosition myPosition;
    public RookMoveCalculator(ChessBoard board, ChessPosition myPosition) {
        super(board, myPosition);
        this.board = board;
        this.myPosition = myPosition;
    }

    Collection<ChessMove> calculateMoves() {
        ChessPiece thisPiece = board.getPiece(myPosition); //stores current piece object in a variable
        Collection<ChessMove> moves = new HashSet<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        //checks a laser in each direction and adds the available spaces in each direction to moves
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"n"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"e"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"s"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"w"));
        return moves;
    }
}

class QueenMoveCalculator extends MoveCalculator {
    private final ChessBoard board;
    private final ChessPosition myPosition;
    public QueenMoveCalculator(ChessBoard board, ChessPosition myPosition) {
        super(board, myPosition);
        this.board = board;
        this.myPosition = myPosition;
    }
    Collection<ChessMove> calculateMoves() {
        ChessPiece thisPiece = board.getPiece(myPosition); //stores current piece object in a variable
        Collection<ChessMove> moves = new HashSet<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        //checks a laser in each direction and adds the available spaces in each direction to moves
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"n"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"ne"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"e"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"se"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"s"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"sw"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"w"));
        moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece,"nw"));
        return moves;
    }
}

class KingMoveCalculator extends MoveCalculator {
    ChessBoard board;
    ChessPosition myPosition;
    public KingMoveCalculator(ChessBoard board, ChessPosition myPosition){
        super(board, myPosition);
        this.board = board;
        this.myPosition = myPosition;
    }

    Collection<ChessMove> calculateMoves() {
        ChessPiece thisPiece = board.getPiece(myPosition); //stores current piece object in a variable
        Collection<ChessMove> moves = new HashSet<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        //checks a single square in each direction and adds the available spaces in each direction to moves
        moves.addAll(checkLaser(myRow, myCol, 1, board, myPosition, thisPiece,"n"));
        moves.addAll(checkLaser(myRow, myCol, 1, board, myPosition, thisPiece,"ne"));
        moves.addAll(checkLaser(myRow, myCol, 1, board, myPosition, thisPiece,"e"));
        moves.addAll(checkLaser(myRow, myCol, 1, board, myPosition, thisPiece,"se"));
        moves.addAll(checkLaser(myRow, myCol, 1, board, myPosition, thisPiece,"s"));
        moves.addAll(checkLaser(myRow, myCol, 1, board, myPosition, thisPiece,"sw"));
        moves.addAll(checkLaser(myRow, myCol, 1, board, myPosition, thisPiece,"w"));
        moves.addAll(checkLaser(myRow, myCol, 1, board, myPosition, thisPiece,"nw"));
        return moves;
    }

}

class PawnMoveCalculator extends MoveCalculator {
    ChessBoard board;
    ChessPosition myPosition;
    public PawnMoveCalculator(ChessBoard board, ChessPosition myPosition){
        super(board, myPosition);
        this.board = board;
        this.myPosition = myPosition;
    }

    Collection<ChessMove> calculateMoves() {
        ChessPiece thisPiece = board.getPiece(myPosition); //stores current piece object in a variable
        Collection<ChessMove> moves = new HashSet<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        int steps;
        if(myRow == 2 || myRow == 7) //ensure that pawns can move 2 spaces if they are on their starter row
            steps = 2;               //a white pawn will trigger this at row 7, but it only has one space left anyway
        else steps = 1;

        //white pawns
        moves.addAll(checkLaser(myRow, myCol, steps, board, myPosition, thisPiece,"n"));
        //black pawns
        moves.addAll(checkLaser(myRow, myCol, steps, board, myPosition, thisPiece,"s"));

        return moves;
    }
}

//class KnightMoveCalculator extends MoveCalculator {
//
//}

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

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece thisPiece = board.getPiece(myPosition); //stores current piece object in a variable
        Collection<ChessMove> moves = new ArrayList<ChessMove>();
        switch(thisPiece.getPieceType()){
            case BISHOP:
                BishopMoveCalculator bishopCalc = new BishopMoveCalculator(board, myPosition);
                moves = bishopCalc.calculateMoves();
                break;
            case ROOK:
                RookMoveCalculator rookCalc = new RookMoveCalculator(board, myPosition);
                moves = rookCalc.calculateMoves();
                break;
            case QUEEN:
                QueenMoveCalculator queenCalc = new QueenMoveCalculator(board, myPosition);
                moves = queenCalc.calculateMoves();
                break;
            case KING:
                KingMoveCalculator kingCalc = new KingMoveCalculator(board, myPosition);
                moves = kingCalc.calculateMoves();
                break;
        }
        return moves;
    }
}
