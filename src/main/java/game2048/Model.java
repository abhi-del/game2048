package game2048;

import java.util.Formatter;


/** The state of a game of 2048.
 *  @author P. N. Hilfinger + Josh Hug
 */
public class Model {
    /** Current contents of the board. */
    private final Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore) {
        board = new Board(rawValues);
        this.score = score;
        this.maxScore = maxScore;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board. */
    public int size() {
        return board.size();
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        board.clear();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        return maxTileExists(board) || !atLeastOneMoveExists(board);
    }

    /** Checks if the game is over and sets the maxScore variable
     *  appropriately.
     */
    private void checkGameOver() {
        if (gameOver()) {
            maxScore = Math.max(score, maxScore);
        }
    }
    
    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        int size = b.size();
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                if (b.tile(col, row) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by this.MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        int size = b.size();
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                if (b.tile(col, row) != null && b.tile(col, row).value() == MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        int size = b.size();
        Tile tile;
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                tile = b.tile(col, row);
                if (tile == null) {
                    return true;
                } else if (checkAdjacentTile(b, row, col)) {
                    return true;
                }
            }
        }


        return false;
    }

    private static boolean checkAdjacentTile(Board b, int row, int col) {

        int value = b.tile(col, row).value();
        int adjRow = row + 1;
        int adjCol = col + 1;
        return (adjCol < b.size() && b.tile(row, adjCol) != null && b.tile(row, adjCol).value() == value)
                || (adjRow < b.size() && b.tile(adjRow, col) != null && b.tile(adjRow, col).value() == value);

    }


    /** Tilt the board toward SIDE.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public void tilt(Side side) {

        // Change the viewing perspective to the SIDE side.
        // So that same logic can be used for all the motions.
        board.setViewingPerspective(side);
        for (int col = 0; col < board.size(); col++) {
            rearrangeColumn(col);
        }
        // Revert the viewing perspective back to Side.NORTH
        board.setViewingPerspective(Side.NORTH);

        checkGameOver();
    }

    /**
     * Method to rearrange cells in the particular column
     * Variable blank is used as pointer to the cell to which the tile should be moved.
     * @param col column whose cells need to be updated after motion
     */
    private void rearrangeColumn(int col) {
        int blank = board.size() - 1;
        for (int row = board.size() - 1; row > -1; row--) {
            Tile tile = board.tile(col, row);
            if (tile != null) {
                if (board.move(col, blank, tile)) {
                    score += 2 * tile.value();
                    continue;
                }
                int target = getRowToMerge(col, row - 1, tile.value());
                // The next non-blank tile is having the same value.
                // Merge and update the score.
                if (target > 0) {
                    board.move(col, blank, board.tile(col, target));
                    score += 2 * tile.value();
                    blank--;
                }
                // The next non-blank tile is having different value so no merge can happen
                // for the current Tile.
                if (target == -2) {
                    blank--;
                }
            }
        }
    }

    /**
     * Method to return the row number of the Tile that has the same value as
     * the current tile in the calling method.
     * @param col column to be iterated.
     * @param row starting row number for iteration.
     * @param value value of the Tile in the calling method.
     * @return
     * -1 if the column below is blank
     * -2 if the next non-blank Tile in the column has different value.
     * ROW_NUMBER if the next non-blank Tile in the column has same value.
     */
    private int getRowToMerge(int col, int row, int value) {
        while (row >= 0 && board.tile(col, row) == null) {
            row--;
        }
        if (row < 0) {
            return -1;
        }

        if (board.tile(col, row).value() == value) {
            return row;
        } else {
            return -2;
        }
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

