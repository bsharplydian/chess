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

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public String toString() {
        String result = switch(type) {
            case KING -> "k";
            case QUEEN -> "q";
            case BISHOP -> "b";
            case KNIGHT -> "n";
            case ROOK -> "r";
            case PAWN -> "p";
        };
        if(pieceColor == ChessGame.TeamColor.WHITE)
            result = result.toUpperCase();
        return result;
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
            case PAWN:
                PawnMoveCalculator pawnCalc = new PawnMoveCalculator(board, myPosition);
                moves = pawnCalc.calculateMoves();
                break;
            case KNIGHT:
                KnightMoveCalculator knightCalc = new KnightMoveCalculator(board, myPosition);
                moves = knightCalc.calculateMoves();
                break;
        }
        return moves;
    }
}

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
    Collection<ChessMove> checkLaser(int row, int column, int range, ChessBoard board, ChessPosition position, ChessPiece thisPiece, int direction) {
        /** 0=north
         * 2=east
         * 4=sout
         * 6=west
         */
        int distance = 1;
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPosition checkPos;
        int counter = range;
        while(true) {
            checkPos = switch (direction) {
                case 1 -> new ChessPosition(row + distance, column + distance);
                case 3 -> new ChessPosition(row - distance, column + distance);
                case 5 -> new ChessPosition(row - distance, column - distance);
                case 7 -> new ChessPosition(row + distance, column - distance);
                case 0 -> new ChessPosition(row + distance, column);
                case 2 -> new ChessPosition(row, column + distance);
                case 4 -> new ChessPosition(row - distance, column);
                case 6 -> new ChessPosition(row, column - distance);
                default -> new ChessPosition(1, 1);
            };
            if(!checkPos.isInBounds() || counter == 0)
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
    Collection<ChessMove> getPromotionMoves(ChessPosition position, ChessPosition checkPos) {
        Collection<ChessMove> moves = new ArrayList<>();
        moves.add(new ChessMove(position, checkPos, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(position, checkPos, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(position, checkPos, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(position, checkPos, ChessPiece.PieceType.KNIGHT));
        return moves;
    }
    Collection<ChessMove> checkSpace(int row, int column, ChessBoard board, ChessPosition position, ChessPiece thisPiece, int rowDiff, int colDiff) {
        Collection<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPosition checkPos = new ChessPosition(row + rowDiff, column + colDiff);
        ChessMove potentialMove = new ChessMove(position, checkPos, null);
        ChessPiece targetPiece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        if(checkPos.getColumn() <= 8 && checkPos.getColumn() >= 1 && checkPos.getRow() <= 8 && checkPos.getRow() >= 1) {
            targetPiece = board.getPiece(checkPos);

            if (thisPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                //pawn special rules

                if (targetPiece != null && colDiff != 0 && targetPiece.getTeamColor() != thisPiece.getTeamColor()) {
                    //capture diagonals

                    if (checkPos.getRow() == 8 || checkPos.getRow() == 1) //add promotions if moving to last rank
                        moves.addAll(getPromotionMoves(position, checkPos));
                    else //use default (null) promotion if moving to any other rank
                        moves.add(potentialMove);
                } else if (colDiff == 0 && targetPiece == null)
                    //move forward

                    if (checkPos.getRow() == 8 || checkPos.getRow() == 1)
                        moves.addAll(getPromotionMoves(position, checkPos));
                    else
                        moves.add(potentialMove);
            } else if (targetPiece == null || targetPiece.getTeamColor() != thisPiece.getTeamColor()) {
                //rules for knights
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
        for(int i = 1; i < 8; i+=2)
            moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece, i));
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
        for(int i = 0; i < 8; i+=2)
            moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece, i));
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
        for(int i = 0; i < 8; i++)
            moves.addAll(checkLaser(myRow, myCol, 8, board, myPosition, thisPiece, i));
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
        for(int i = 0; i < 8; i++)
            moves.addAll(checkLaser(myRow, myCol, 1, board, myPosition, thisPiece, i));
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

    Collection<ChessMove> choosePawnDirection(int myRow, int myCol, ChessBoard board, ChessPosition myPosition, ChessPiece thisPiece, int direction){
        Collection<ChessMove> moves = new ArrayList<>();
        //directly ahead
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, direction, 0));
        boolean firstSpacePossible = !moves.isEmpty();
        //if in starting position, check second space ahead (and make sure that it can move to the first space)
        if(((direction == 1 && myRow == 2) || (direction == -1 && myRow == 7)) && firstSpacePossible)
            moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, 2*direction, 0));

        //check diagonals for capture
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, direction, 1));
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, direction, -1));

        return moves;
    }
    Collection<ChessMove> calculateMoves() {
        ChessPiece thisPiece = board.getPiece(myPosition); //stores current piece object in a variable
        Collection<ChessMove> moves = new HashSet<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        int direction = 1;

        if(board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) { //white pawns
            direction = 1;

        } else if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.BLACK) { //black pawns
            direction = -1;
        }
        moves.addAll(choosePawnDirection(myRow, myCol, board, myPosition, thisPiece, direction));

        return moves;
    }
}

class KnightMoveCalculator extends MoveCalculator {
    private final ChessBoard board;
    private final ChessPosition myPosition;

    public KnightMoveCalculator(ChessBoard board, ChessPosition myPosition){
        super(board, myPosition);

        this.board = board;
        this.myPosition = myPosition;
    }

    Collection<ChessMove> calculateMoves() {
        ChessPiece thisPiece = board.getPiece(myPosition);
        Collection<ChessMove> moves = new ArrayList<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, 1, 2));
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, -1, 2));
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, 1, -2));
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, -1, -2));
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, 2, 1));
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, -2, 1));
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, 2, -1));
        moves.addAll(checkSpace(myRow, myCol, board, myPosition, thisPiece, -2, -1));
        return moves;
    }
}
