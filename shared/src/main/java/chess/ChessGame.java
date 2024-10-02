package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor turn = TeamColor.WHITE;
    ChessBoard board = new ChessBoard();
    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> moves = new ArrayList<>();
        if(piece == null) return null;
        if(piece.getTeamColor() != turn)
            return moves; //0. ensure that the selected piece is on the current team

        moves = piece.pieceMoves(board, startPosition); //1. call pieceMoves on the start position


        //2. remove any move that would put (or leave) the current team's king in check


            //a. revealed checks: see if a bishop, rook, or queen is threatening your position,
                //then see if there is a king of your color in the opposite direction
            //b. ignored checks: run isInCheck, then only allow moves that will block (or capture) the threat
        for(var move : moves) {
            ChessBoard hypotheticalBoard = new ChessBoard();
            hypotheticalBoard.setSquares(board.getSquares());
            board.getPiece(move.getStartPosition());
        }
        //2 revised. remove any move that would put (or leave) the current team's king in check
            //a. make a copy of the chessboard and do the move
            //b. if isInCheck() != true on the copied move, leave the move in
        //3. return the resulting collection


        //revealed checks: top priority, if you move your king will die
            //but you COULD move and stay in the line of fire
        //ignored checks: second priority, if you move TO THE WRONG SPOT your king will die
        return moves;
    }
    //helper function:
    //boolean willShootFoot() {
        // takes as input a potential chessMove, then determines whether it will or won't leave the king in check
    //}
    boolean checkLaser(ChessPosition startPosition, int addRow, int addCol) {
        int distance = 1;
        int myRow = startPosition.getRow();
        int myCol = startPosition.getColumn();
        ChessPiece piece = board.getPiece(startPosition);

        while(true) {
            ChessPosition checkPos = new ChessPosition(myRow+addRow*distance, myCol+addCol*distance);
            if (!checkPos.isInBounds() || board.getPiece(checkPos).getTeamColor() == piece.getTeamColor()) //found an edge or piece of your own team
                return false;
            if(board.getPiece(checkPos) != null) {   //there is in fact a piece there
                ChessPiece.PieceType threatType = board.getPiece(checkPos).getPieceType();
                if (Math.abs(addRow) == Math.abs(addCol)) { // diagonal
                    return threatType == ChessPiece.PieceType.BISHOP || threatType == ChessPiece.PieceType.QUEEN;
                } else { // cardinal
                    return threatType == ChessPiece.PieceType.ROOK || threatType == ChessPiece.PieceType.QUEEN;
                }
            }
            distance++;

        }
    }
    boolean checkForKing(ChessPosition startPosition, int addRow, int addCol) {
        int distance = 1;
        int myRow = startPosition.getRow();
        int myCol = startPosition.getColumn();
        ChessPiece piece = board.getPiece(startPosition);

        while(true) {
            ChessPosition checkPos = new ChessPosition(myRow + addRow * distance, myCol + addCol * distance);
            if (!checkPos.isInBounds() || board.getPiece(checkPos).getTeamColor() != piece.getTeamColor()) //found an edge or piece of your own team
                return false;
            if (board.getPiece(checkPos) != null) {
                ChessPiece.PieceType otherType = board.getPiece(checkPos).getPieceType();
                return otherType == ChessPiece.PieceType.KING;
            }
            distance++;
        }
    }
    boolean cardinalThreat(ChessPosition startPosition) {
        if(checkLaser(startPosition, 0, 1))
            return checkForKing(startPosition, 0, -1);
        if(checkLaser(startPosition, 0, -1))
            return checkForKing(startPosition, 0, 1);
        if(checkLaser(startPosition, 1, 0))
            return checkForKing(startPosition, -1, 0);
        if(checkLaser(startPosition, -1, 0))
            return checkForKing(startPosition, 1, 0);
        return false;
    }
    boolean diagonalThreat(ChessPosition startPosition) {
        return false;
    }
    boolean pinned(ChessPosition startPosition, ChessPosition threatPosition){
        return false;
    }
    public Collection<ChessMove> getAllMoves(TeamColor teamColor) {
        Collection<ChessMove> moves = new ArrayList<>();
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++) {
                ChessPosition checkPos = new ChessPosition(i, j);
                if(board.getPiece(checkPos).getTeamColor() == teamColor) {
                    moves.addAll(validMoves(checkPos));
                }
            }
        }
        return moves;
    }
    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");

        //takes a move, ensures that it is valid, then executes it

        //1. call validMoves on the start position
        //2. if the resulting list of moves contains the given move:
        //3. remove the piece from the start position
        //4. replace any piece at the end position with the piece that was at the start position
        //5. check if the piece is now threatening the king or has revealed a threat to the king
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
        //1. check every piece on the opposing team
        //2. as soon as you reach a move that ends on the king's square, return true
        //no no no there's gotta be a better way

        //revised version:
        //return boolean black_check or white_check; these values will be updated when a piece makes its move

        // what if we store a boolean for each team
        // logic goes in a separate function:
        // if you make a move, check your own next moves and those of any rook, bishop, or queen on your team that was attacking your position
            //this should account for both direct and revealed checks
            //find any of them: check in the (appropriate) opposite direction for a king of the other color
            //2 functions: revealDiagonals and revealUprights

        //no no no no there's an even easier way
        //1. start at the king's space
        //2. check cardinals for rooks and queens
        //3. check diagonals for bishops and queens
        //4. check two corners in correct direction for pawns
        //5. check 8 possible knight spaces
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
        //1. check if isInCheck is true
        //2. check every piece on your team, and if there are no legal moves, return true
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor))
            return false;
        return getAllMoves(teamColor).isEmpty();
        //1. ensure that isInCheck is false
        //2. check every piece on your team, and if there are no legal moves, return true.
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
