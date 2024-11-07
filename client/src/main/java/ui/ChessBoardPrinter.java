package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Random;

import static ui.EscapeSequences.*;

public class ChessBoardPrinter {
    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 3;
    private static final int LINE_WIDTH_IN_PADDED_CHARS = 1;

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


    private static Random rand = new Random();


    public static void displayBoard(ChessBoard board) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);

        //drawHeaders(out);

        drawChessBoard(out, board);
//        drawChessBoardBlack(out, board);

        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void drawHeaders(PrintStream out) {

        setBlack(out);

        String[] headers = { "TIC", "TAC", "TOE" };
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            drawHeader(out, headers[boardCol]);

            if (boardCol < BOARD_SIZE_IN_SQUARES - 1) {
                out.print(EMPTY.repeat(LINE_WIDTH_IN_PADDED_CHARS));
            }
        }

        out.println();
    }

    private static void drawHeader(PrintStream out, String headerText) {
        int prefixLength = SQUARE_SIZE_IN_PADDED_CHARS / 2;
        int suffixLength = SQUARE_SIZE_IN_PADDED_CHARS - prefixLength - 1;

        out.print(EMPTY.repeat(prefixLength));
        printHeaderText(out, headerText);
        out.print(EMPTY.repeat(suffixLength));
    }

    private static void printHeaderText(PrintStream out, String player) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_GREEN);

        out.print(player);

        setBlack(out);
    }
    private static void drawChessBoard(PrintStream out, ChessBoard board) {

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
            if(row > 0) {
                setBlank(out);
                out.print("\n");
            }

        }
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
    private static void drawChessBoardBlack(PrintStream out, ChessBoard board) {
        for(int boardRow = BOARD_SIZE_IN_SQUARES-1; boardRow > 0; --boardRow) {
            drawRowBackward(out, board.getSquares()[boardRow]);
            if(boardRow > 1) {
                out.print("\n");
                setBlack(out);
            }
        }
    }
    private static void drawRow(PrintStream out, ChessPiece[] pieces) {
        Boolean blackChecker = true;
        for(var piece : pieces) {
            if(piece == null) {
                out.print(EMPTY);
            } else {
                setBlue(out);
                out.print(SET_TEXT_COLOR_BLACK);
                out.print(piece);
                setBlack(out);
            }
        }
        setBlank(out);

    }
    private static void drawRowBackward(PrintStream out, ChessPiece[] pieces) {
        for(int i = pieces.length - 1; i > 0; i++) {
            if(pieces[i] == null) {
                out.print(EMPTY);
            } else {
                setBlue(out);
                out.print(SET_TEXT_COLOR_BLACK);
                out.print(pieces[i]);
                setBlack(out);
            }
        }
    }
//    private static void drawRowOfSquares(PrintStream out) {
//
//        for (int squareRow = 0; squareRow < SQUARE_SIZE_IN_PADDED_CHARS; ++squareRow) {
//            for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
//                setWhite(out);
//
//                if (squareRow == SQUARE_SIZE_IN_PADDED_CHARS / 2) {
//                    int prefixLength = SQUARE_SIZE_IN_PADDED_CHARS / 2;
//                    int suffixLength = SQUARE_SIZE_IN_PADDED_CHARS - prefixLength - 1;
//
//                    out.print(EMPTY.repeat(prefixLength));
//                    printPlayer(out, rand.nextBoolean() ? X : O);
//                    out.print(EMPTY.repeat(suffixLength));
//                }
//                else {
//                    out.print(EMPTY.repeat(SQUARE_SIZE_IN_PADDED_CHARS));
//                }
//
//                if (boardCol < BOARD_SIZE_IN_SQUARES - 1) {
//                    // Draw vertical column separator.
//                    setRed(out);
//                    out.print(EMPTY.repeat(LINE_WIDTH_IN_PADDED_CHARS));
//                }
//
//                setBlack(out);
//            }
//
//            out.println();
//        }
//    }

    private static void drawHorizontalLine(PrintStream out) {

        int boardSizeInSpaces = BOARD_SIZE_IN_SQUARES * SQUARE_SIZE_IN_PADDED_CHARS +
                (BOARD_SIZE_IN_SQUARES - 1) * LINE_WIDTH_IN_PADDED_CHARS;

        for (int lineRow = 0; lineRow < LINE_WIDTH_IN_PADDED_CHARS; ++lineRow) {
            setRed(out);
            out.print(EMPTY.repeat(boardSizeInSpaces));

            setBlack(out);
            out.println();
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
    private static void printPlayer(PrintStream out, String player) {
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_BLACK);

        out.print(player);

        setWhite(out);
    }

}
