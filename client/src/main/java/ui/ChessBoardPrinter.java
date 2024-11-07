package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class ChessBoardPrinter {
    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;

    // Padded characters.
    private static final String EMPTY = "   ";
    private static final String KingW = " K ";
    private static final String QueenW = " Q ";
    private static final String BishopW = " B ";
    private static final String KnightW = " N ";
    private static final String RookW = " R ";
    private static final String PawnW = " P ";
    private static final String KingB = " k ";
    private static final String QueenB = " q ";
    private static final String BishopB = " b ";
    private static final String KnightB = " n ";
    private static final String RookB = " r ";
    private static final String PawnB = " p ";


    public static void displayBoard(ChessBoard board) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);

        drawChessBoard(out, board);
        out.print("\n");
        drawChessBoardBlack(out, board);

        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }


    private static void drawChessBoard(PrintStream out, ChessBoard board) {
        drawColumnLetters(out);
        for(int row = BOARD_SIZE_IN_SQUARES - 1; row >= 0; --row) {
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(SET_TEXT_COLOR_BLACK);
            out.printf(" %d ", row+1);
            for(int col = 0; col < BOARD_SIZE_IN_SQUARES; ++col) {
                if((row + col)%2 == 1){
                    out.print(SET_BG_COLOR_WHITE);
                } else {
                    out.print(SET_BG_COLOR_BLUE);
                }
                drawSquare(out, board.getSquares()[row][col]);
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
                if((row + col)%2 == 1){
                    out.print(SET_BG_COLOR_WHITE);
                } else {
                    out.print(SET_BG_COLOR_BLUE);
                }
                drawSquare(out, board.getSquares()[row][col]);
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
    private static void drawSquare(PrintStream out, ChessPiece piece) {
        String output;
        if(piece == null) {
            out.print(EMPTY);
        } else {
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                output = switch (piece.getPieceType()) {
                    case KING -> KingW;
                    case QUEEN -> QueenW;
                    case BISHOP -> BishopW;
                    case KNIGHT -> KnightW;
                    case ROOK -> RookW;
                    case PAWN -> PawnW;
                };
            } else {
                output = switch (piece.getPieceType()) {
                    case KING -> KingB;
                    case QUEEN -> QueenB;
                    case BISHOP -> BishopB;
                    case KNIGHT -> KnightB;
                    case ROOK -> RookB;
                    case PAWN -> PawnB;
                };
            }
            out.print(output);
        }
    }


    private static void setWhite(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setRed(PrintStream out) {
        out.print(SET_BG_COLOR_RED);
        out.print(SET_TEXT_COLOR_RED);
    }

    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setBlue(PrintStream out) {
        out.print(SET_BG_COLOR_BLUE);
        out.print(SET_TEXT_COLOR_BLUE);
    }

    private static void setBlank(PrintStream out) {
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(RESET_BG_COLOR);
    }

}
