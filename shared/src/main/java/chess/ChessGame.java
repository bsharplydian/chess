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
        BLACK,
        NONE
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
        Collection<ChessMove> moves;
        if (piece == null) {
            return null;
        }

        moves = piece.pieceMoves(board, startPosition);

        TeamColor color = piece.getTeamColor();
        Collection<ChessMove> legalMoves = new ArrayList<>();
        for (var move : moves) {

            ChessBoard storageBoard = new ChessBoard();

            ChessPiece[][] boardDeepCopy = new ChessPiece[8][8];

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    ChessPosition copyPos = new ChessPosition(i + 1, j + 1);
                    if (board.getPiece(copyPos) == null) {
                        continue;
                    }
                    ChessPiece newPiece = new ChessPiece(board.getPiece(copyPos).getTeamColor(), board.getPiece(copyPos).getPieceType());

                    boardDeepCopy[i][j] = newPiece;
                }
            }


            storageBoard.setSquares(boardDeepCopy);
            board.movePiece(move);
            if (!isInCheck(color)) {
                legalMoves.add(move);
            }
            board.setSquares(storageBoard.getSquares());
        }

        return legalMoves;
    }

    boolean checkLaser(ChessPosition startPosition, int addRow, int addCol) {
        //returns true if there is a bishop, rook or queen threatening your position from the given direction
        int distance = 1;
        int myRow = startPosition.getRow();
        int myCol = startPosition.getColumn();
        ChessPiece piece = board.getPiece(startPosition);

        while (true) {
            ChessPosition checkPos = new ChessPosition(myRow + addRow * distance, myCol + addCol * distance);
            if (!checkPos.isInBounds()) //found an edge or piece of your own team
            {
                return false;
            }
            if (board.getPiece(checkPos) != null) {   //there is in fact a piece there
                if (board.getPiece(checkPos).getTeamColor() == piece.getTeamColor()) {
                    return false;
                }
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
        return checkLaser(startPosition, 0, 1) ||
                checkLaser(startPosition, 0, -1) ||
                checkLaser(startPosition, 1, 0) ||
                checkLaser(startPosition, -1, 0);
    }

    boolean diagonalThreat(ChessPosition startPosition) {
        return checkLaser(startPosition, 1, 1) ||
                checkLaser(startPosition, 1, -1) ||
                checkLaser(startPosition, -1, 1) ||
                checkLaser(startPosition, -1, -1);
    }

    public Collection<ChessMove> getAllMoves(TeamColor teamColor) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition checkPos = new ChessPosition(i, j);
                if (board.getPiece(checkPos) == null) {
                    continue;
                }
                if (board.getPiece(checkPos).getTeamColor() == teamColor) {
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
        if (legalMoves == null || legalMoves.isEmpty()) {
            throw new InvalidMoveException("not a legal move");
        }
        if (board.getPiece(move.getStartPosition()).getTeamColor() != turn) {
            throw new InvalidMoveException("not a legal move");
        }
        if (legalMoves.contains(move)) {
            board.movePiece(move);
            turn = switch (board.getPiece(move.getEndPosition()).getTeamColor()) {
                case WHITE -> TeamColor.BLACK;
                case BLACK -> TeamColor.WHITE;
                case NONE -> TeamColor.NONE;
            };
        } else {
            throw new InvalidMoveException();
        }
        //takes a move, ensures that it is valid, then executes it

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    private boolean checkSpace(int row, int col, TeamColor teamColor, ChessPiece.PieceType type) {
        ChessPosition checkPos = new ChessPosition(row, col);
        if (!checkPos.isInBounds() || board.getPiece(checkPos) == null) {
            return false;
        }

        return board.getPiece(checkPos).getTeamColor() != teamColor && board.getPiece(checkPos).getPieceType() == type;
    }

    private boolean pawnKnightThreat(ChessPosition kingPos, TeamColor teamColor) {
        int kingRow = kingPos.getRow();
        int kingCol = kingPos.getColumn();
        int pawnThreatDirection = switch (teamColor) { //pawns will threaten from different sides
            case WHITE -> 1;
            case BLACK -> -1;
            case NONE -> 1;
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
                checkSpace(kingRow - 1, kingCol - 2, teamColor, ChessPiece.PieceType.KNIGHT) ||

                checkSpace(kingRow - 1, kingCol + 1, teamColor, ChessPiece.PieceType.KING) ||
                checkSpace(kingRow, kingCol + 1, teamColor, ChessPiece.PieceType.KING) ||
                checkSpace(kingRow + 1, kingCol + 1, teamColor, ChessPiece.PieceType.KING) ||
                checkSpace(kingRow - 1, kingCol, teamColor, ChessPiece.PieceType.KING) ||
                checkSpace(kingRow + 1, kingCol, teamColor, ChessPiece.PieceType.KING) ||
                checkSpace(kingRow - 1, kingCol - 1, teamColor, ChessPiece.PieceType.KING) ||
                checkSpace(kingRow, kingCol - 1, teamColor, ChessPiece.PieceType.KING) ||
                checkSpace(kingRow + 1, kingCol - 1, teamColor, ChessPiece.PieceType.KING);

    }

    public boolean isInCheck(TeamColor teamColor) {

        ChessPosition kingPos = findKing(teamColor);

        if (kingPos == null) {
            return false;
        } else {
            return cardinalThreat(kingPos) || diagonalThreat(kingPos) || pawnKnightThreat(kingPos, teamColor);
        }
    }

    private ChessPosition findKing(TeamColor color) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition checkPos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(checkPos);
                if (board.getPiece(checkPos) == null) {
                    continue;
                }
                if ((piece.getPieceType() == ChessPiece.PieceType.KING) && piece.getTeamColor() == color) {
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
        return getAllMoves(teamColor).isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
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
