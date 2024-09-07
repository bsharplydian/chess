package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
    /* override toString
    */
    @Override
    public String toString() {
        String letter = "";
        switch(getColumn()){
            case 0:
                letter = "NEVER APPEARS";
                break;
            case 1:
                letter = "a";
                break;
            case 2:
                letter = "b";
                break;
            case 3:
                letter = "c";
                break;
            case 4:
                letter = "d";
                break;
            case 5:
                letter = "e";
                break;
            case 6:
                letter = "f";
                break;
            case 7:
                letter = "g";
                break;
            case 8:
                letter = "h";
                break;
        }
        return letter + getRow();
    }
    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
