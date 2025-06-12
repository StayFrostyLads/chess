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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
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

    public String toAlgebraic() {
        char file = (char) ('a' + (col - 1));
        int rank = row;
        return "" + file + rank;
    }

    public static ChessPosition fromAlgebraic(String algebraicString) {
        if (algebraicString == null || algebraicString.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic coordinates: " + algebraicString);
        }
        char file = algebraicString.charAt(0);
        char rank = algebraicString.charAt(1);
        int col = file - 'a' + 1;
        int row = rank - '0';
        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Algebraic coordinates out of bounds: " + algebraicString);
        }
        return new ChessPosition(row, col);
    }

}