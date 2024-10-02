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


        moves = piece.pieceMoves(board, startPosition); //1. call pieceMoves on the start position


        //2. remove any move that would put (or leave) the current team's king in check


            //a. revealed checks: see if a bishop, rook, or queen is threatening your position,
                //then see if there is a king of your color in the opposite direction
            //b. ignored checks: run isInCheck, then only allow moves that will block (or capture) the threat
        TeamColor color = piece.getTeamColor();
        Collection<ChessMove> legalMoves = new ArrayList<>();
        for(var move : moves) {

            ChessBoard storageBoard = new ChessBoard();
            storageBoard.setSquares(board.getSquares());
            board.movePiece(move);
            if(!isInCheck(color))
                legalMoves.add(move);
            board.setSquares(storageBoard.getSquares());
        }
        //2 revised. remove any move that would put (or leave) the current team's king in check
            //a. make a copy of the chessboard and do the move
            //b. if isInCheck() != true on the copied move, leave the move in
        //3. return the resulting collection


        //revealed checks: top priority, if you move your king will die
            //but you COULD move and stay in the line of fire
        //ignored checks: second priority, if you move TO THE WRONG SPOT your king will die
        return legalMoves;
    }

    boolean checkLaser(ChessPosition startPosition, int addRow, int addCol) {
        //returns true if there is a bishop, rook or queen threatening your position from the given direction
        int distance = 1;
        int myRow = startPosition.getRow();
        int myCol = startPosition.getColumn();
        ChessPiece piece = board.getPiece(startPosition);

        while(true) {
            ChessPosition checkPos = new ChessPosition(myRow+addRow*distance, myCol+addCol*distance);
            if (!checkPos.isInBounds()) //found an edge or piece of your own team
                return false;
            if(board.getPiece(checkPos) != null) {   //there is in fact a piece there
                if(board.getPiece(checkPos).getTeamColor() == piece.getTeamColor())
                    return false;
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

    boolean cardinalThreat(ChessPosition startPosition) {
        if(checkLaser(startPosition, 0, 1) ||
            checkLaser(startPosition, 0, -1) ||
            checkLaser(startPosition, 1, 0) ||
            checkLaser(startPosition, -1, 0)) {
            return true;
        } else {
            return false;
        }
    }
    boolean diagonalThreat(ChessPosition startPosition) {
        if(checkLaser(startPosition, 1, 1) ||
                checkLaser(startPosition, 1, -1) ||
                checkLaser(startPosition, -1, 1) ||
                checkLaser(startPosition, -1, -1)) {
            return true;
        } else {
            return false;
        }
    }

    public Collection<ChessMove> getAllMoves(TeamColor teamColor) {
        Collection<ChessMove> moves = new ArrayList<>();
        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8; j++) {
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
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if(legalMoves == null || legalMoves.isEmpty())
            throw new InvalidMoveException();
        if(board.getPiece(move.getStartPosition()).getTeamColor() != turn)
            throw new InvalidMoveException();
        if(legalMoves.contains(move))
            board.movePiece(move);
        else throw new InvalidMoveException();
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
    private boolean checkSpace(int row, int col, TeamColor teamColor, ChessPiece.PieceType type) {
        ChessPosition checkPos = new ChessPosition(row, col);
        if(!checkPos.isInBounds() || board.getPiece(checkPos) == null)
            return false;

        return board.getPiece(checkPos).getTeamColor() != teamColor && board.getPiece(checkPos).getPieceType() == type;
    }
    private boolean pawnKnightThreat(ChessPosition kingPos, TeamColor teamColor) {
        int kingRow = kingPos.getRow();
        int kingCol = kingPos.getColumn();
        int pawnThreatDirection = switch(teamColor) { //pawns will threaten from different sides
            case WHITE -> 1;
            case BLACK -> -1;
        };


        return checkSpace(kingRow + pawnThreatDirection, kingCol + 1, teamColor, ChessPiece.PieceType.PAWN) ||
                checkSpace(kingRow + pawnThreatDirection, kingCol - 1, teamColor, ChessPiece.PieceType.PAWN) ||
                checkSpace(kingRow + 2, kingCol + 1, teamColor, ChessPiece.PieceType.KNIGHT) ||
                checkSpace(kingRow + 2, kingCol - 1, teamColor, ChessPiece.PieceType.KNIGHT) ||
                checkSpace(kingRow - 2, kingCol + 1, teamColor, ChessPiece.PieceType.KNIGHT) ||
                checkSpace(kingRow - 2, kingCol - 1, teamColor, ChessPiece.PieceType.KNIGHT) ||
                checkSpace(kingRow + 1, kingCol + 2, teamColor, ChessPiece.PieceType.KNIGHT) ||
                checkSpace(kingRow + 1, kingCol - 2, teamColor, ChessPiece.PieceType.KNIGHT) ||
                checkSpace(kingRow - 1, kingCol + 2, teamColor, ChessPiece.PieceType.KNIGHT) ||
                checkSpace(kingRow - 1, kingCol - 2, teamColor, ChessPiece.PieceType.KNIGHT);

    }
    public boolean isInCheck(TeamColor teamColor) {

        ChessPosition kingPos = findKing(teamColor);

        if(kingPos == null) return false;
        else {
            return cardinalThreat(kingPos) || diagonalThreat(kingPos) || pawnKnightThreat(kingPos, teamColor);
        }
    }
    private ChessPosition findKing(TeamColor color) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition checkPos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(checkPos);
                if(board.getPiece(checkPos) == null)
                    continue;
                if((piece.getPieceType() == ChessPiece.PieceType.KING) && piece.getTeamColor() == color) {
                    return checkPos;
                }
            }
        }
        return null;
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
