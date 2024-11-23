package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static client.EscapeSequences.*;

public class ChessBoardPrinter {
    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;

    // Padded characters.
    private static final String EMPTY = "   ";
    private static final String EMPTYWIDE = " \u2003 ";
    private static final String KING_W = " K ";
    private static final String QUEEN_W = " Q ";
    private static final String BISHOP_W = " B ";
    private static final String KNIGHT_W = " N ";
    private static final String ROOK_W = " R ";
    private static final String PAWN_W = " P ";
    private static final String KING_B = " k ";
    private static final String QUEEN_B = " q ";
    private static final String BISHOP_B = " b ";
    private static final String KNIGHT_B = " n ";
    private static final String ROOK_B = " r ";
    private static final String PAWN_B = " p ";


    public static String displayBoard(ChessBoard board, String teamColor) {
        var os = new ByteArrayOutputStream();
        var out = new PrintStream(os, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);
        if(Objects.equals(teamColor, "BLACK")) {
            drawChessBoardBlack(out, board);
        } else {
            drawChessBoardWhite(out, board);
        }


        return os.toString();
    }


    private static void drawChessBoardWhite(PrintStream out, ChessBoard board) {
        drawColumnLetters(out);
        for(int row = BOARD_SIZE_IN_SQUARES - 1; row >= 0; --row) {
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(SET_TEXT_COLOR_BLACK);
            out.printf(" %d ", row+1);
            for(int col = 0; col < BOARD_SIZE_IN_SQUARES; ++col) {
                drawSquare(out, row, col, board.getSquares()[row][col]);
            }
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.printf(" %d ", row+1);
            setBlank(out);
            out.print("\n");

        }
        drawColumnLetters(out);
    }
    private static void drawChessBoardBlack(PrintStream out, ChessBoard board) {
        drawColumnLettersBlack(out);
        for(int row = 0; row < BOARD_SIZE_IN_SQUARES; ++row) {
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(SET_TEXT_COLOR_BLACK);
            out.printf(" %d ", row+1);
            for(int col = BOARD_SIZE_IN_SQUARES-1; col >= 0; --col) {
                drawSquare(out, row, col, board.getSquares()[row][col]);
            }
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.printf(" %d ", row+1);
            setBlank(out);
            out.print("\n");

        }
        drawColumnLettersBlack(out);
    }

    private static void drawColumnLetters(PrintStream out) {
        String output;
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(EMPTY);
        for(int i = 0; i < BOARD_SIZE_IN_SQUARES; i++) {
            output = convertNumToLetter(i);
            out.print(output);
        }
        out.print(EMPTY);
        out.print(RESET_BG_COLOR);
        out.print("\n");
    }
    private static void drawColumnLettersBlack(PrintStream out) {
        String output;
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(EMPTY);
        for(int i = BOARD_SIZE_IN_SQUARES-1; i >=0; i--) {
            output = convertNumToLetter(i);
            out.print(output);
        }
        out.print(EMPTY);
        out.print(RESET_BG_COLOR);
        out.print("\n");
    }

    private static String convertNumToLetter(int i) {
        return switch(i) {
            case 0 -> " a ";
            case 1 -> " b ";
            case 2 -> " c ";
            case 3 -> " d ";
            case 4 -> " e ";
            case 5 -> " f ";
            case 6 -> " g ";
            case 7 -> " h ";
            default -> EMPTY;
        };
    }
    private static void drawSquare(PrintStream out, int row, int col, ChessPiece piece) {
        String output;
        if((row + col)%2 == 1){
            out.print(SET_BG_COLOR_WHITE);
        } else {
            out.print(SET_BG_COLOR_BLUE);
        }
        if(piece == null) {
            out.print(EMPTY);
        } else {
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                output = switch (piece.getPieceType()) {
                    case KING -> KING_W;
                    case QUEEN -> QUEEN_W;
                    case BISHOP -> BISHOP_W;
                    case KNIGHT -> KNIGHT_W;
                    case ROOK -> ROOK_W;
                    case PAWN -> PAWN_W;
                };
            } else {
                output = switch (piece.getPieceType()) {
                    case KING -> KING_B;
                    case QUEEN -> QUEEN_B;
                    case BISHOP -> BISHOP_B;
                    case KNIGHT -> KNIGHT_B;
                    case ROOK -> ROOK_B;
                    case PAWN -> PAWN_B;
                };
            }
            out.print(output);
        }
    }

    private static void setBlank(PrintStream out) {
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(RESET_BG_COLOR);
    }

}
