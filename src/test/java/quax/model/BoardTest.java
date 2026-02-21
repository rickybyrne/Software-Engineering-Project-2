package quax.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    @Test
    void boardStartsWithEmptyPlayableCells() {
        Board board = new Board();

        assertTrue(board.isOctEmpty(0, 0));
        assertTrue(board.isOctEmpty(10, 10));
        assertTrue(board.isRhombEmpty(0, 0));
        assertTrue(board.isRhombEmpty(9, 9));
    }

    @Test
    void inBoundsChecksMatchBoardSizes() {
        Board board = new Board();

        assertTrue(board.isOctInBounds(10, 10));
        assertFalse(board.isOctInBounds(11, 0));

        assertTrue(board.isRhombInBounds(9, 9));
        assertFalse(board.isRhombInBounds(10, 0));
    }
}
