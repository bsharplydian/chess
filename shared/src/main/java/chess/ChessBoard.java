package chess;

import java.util.Arrays;
import java.util.Objects;
import chess.ChessPiece.PieceType;
import chess.ChessGame.TeamColor;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;

    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    public ChessPiece[][] getSquares() {
        return squares;
    }
    public void setSquares(ChessPiece[][] squares) {
        this.squares = squares;
    }
    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    private void setTeamPieces(TeamColor color) {
        int rank = 0;
        if(color == TeamColor.BLACK)
            rank = 7;
        squares[rank][0] = new ChessPiece(color, PieceType.ROOK);
        squares[rank][1] = new ChessPiece(color, PieceType.KNIGHT);
        squares[rank][2] = new ChessPiece(color, PieceType.BISHOP);
        squares[rank][3] = new ChessPiece(color, PieceType.QUEEN);
        squares[rank][4] = new ChessPiece(color, PieceType.KING);
        squares[rank][5] = new ChessPiece(color, PieceType.BISHOP);
        squares[rank][6] = new ChessPiece(color, PieceType.KNIGHT);
        squares[rank][7] = new ChessPiece(color, PieceType.ROOK);

        //front rank
        rank = 1;
        if(color == TeamColor.BLACK)
            rank = 6;
        for(int i = 0; i < 8; i++)
            squares[rank][i] = new ChessPiece(color, PieceType.PAWN);
    }
    public void resetBoard() {
        var white = ChessGame.TeamColor.WHITE;
        var black = ChessGame.TeamColor.BLACK;

        setTeamPieces(white);
        setTeamPieces(black);


    }
    public void movePiece(ChessMove move) {
        //doesn't think about the move at all, just takes a piece and puts it in a destination, replacing whatever is there
        ChessPiece myPiece = getPiece(move.getStartPosition());
        if(move.getPromotionPiece() != null)
            myPiece.setPieceType(move.getPromotionPiece());
        this.addPiece(move.getEndPosition(), myPiece);
        this.addPiece(move.getStartPosition(), null);
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();
        for(int i = 7; i >= 0; i--) {
            for(int j = 0; j < 8; j++) {
                build.append("|");
                if(squares[i][j] != null)
                    build.append(squares[i][j].toString());
                else build.append(" ");
            }
            build.append("|\n");
        }
        return build.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
