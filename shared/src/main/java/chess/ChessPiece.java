package chess;

import java.util.ArrayList;
import java.util.Collection;

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
        throw new RuntimeException("Not implemented");
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

        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        switch(thisPiece.getPieceType()) {
            case BISHOP:
                int distance = 1;
                // create 4 diagonal "lasers" that stop when they hit another piece or the edge
                // up and right
                while(myRow + distance <= 8 && myCol + distance <= 8) {
                    ChessPosition checkPos = new ChessPosition(myRow + distance, myCol + distance);
                    ChessMove potentialMove = new ChessMove(myPosition, checkPos, getPieceType());
                    if(board.getPiece(checkPos) == null || board.getPiece(checkPos).getTeamColor() != thisPiece.getTeamColor()){
                        //if the selected square is empty or has a piece from the opposite team, add the move to the move list
                        moves.add(potentialMove);
                        distance += 1;
                    }
                    else
                        break;
                }
                //down and right
                distance = 1;
                while(myRow + distance <= 8 && myCol - distance > 0) {
                    ChessPosition checkPos = new ChessPosition(myRow + distance, myCol - distance);
                    ChessMove potentialMove = new ChessMove(myPosition, checkPos, getPieceType());
                    if(board.getPiece(checkPos) == null || board.getPiece(checkPos).getTeamColor() != thisPiece.getTeamColor()){
                        //if the selected square is empty or has a piece from the opposite team, add the move to the move list
                        moves.add(potentialMove);
                        distance += 1;
                    }
                    else
                        break;
                }
                //down and left
                distance = 1;
                while(myRow - distance > 0 && myCol - distance > 0) {
                    ChessPosition checkPos = new ChessPosition(myRow - distance, myCol - distance);
                    ChessMove potentialMove = new ChessMove(myPosition, checkPos, getPieceType());
                    if(board.getPiece(checkPos) == null || board.getPiece(checkPos).getTeamColor() != thisPiece.getTeamColor()){
                        //if the selected square is empty or has a piece from the opposite team, add the move to the move list
                        moves.add(potentialMove);
                        distance += 1;
                    }
                    else
                        break;
                }
                distance = 1;
                while(myRow - distance > 0 && myCol + distance <= 8) {
                    ChessPosition checkPos = new ChessPosition(myRow - distance, myCol + distance);
                    ChessMove potentialMove = new ChessMove(myPosition, checkPos, getPieceType());
                    if(board.getPiece(checkPos) == null || board.getPiece(checkPos).getTeamColor() != thisPiece.getTeamColor()){
                        //if the selected square is empty or has a piece from the opposite team, add the move to the move list
                        moves.add(potentialMove);
                        distance += 1;
                    }
                    else
                        break;
                }
        }
        return moves;
    }
}
