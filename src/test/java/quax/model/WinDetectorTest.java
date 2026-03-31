package quax.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WinDetectorTest {

    @Test
    void detectsBlackTopToBottomWinThroughOrthogonalStones() {
        Board board = new Board();
        WinDetector detector = new WinDetector();

        for (int row = 0; row < 11; row++) {
            board.placeStone(row, 0, PlayerColor.BLACK);
        }

        assertEquals(PlayerColor.BLACK, detector.checkWinner(board));
    }

    @Test
    void detectsWhiteLeftToRightWinThroughRhombLinkedDiagonalChain() {
        Board board = new Board();
        WinDetector detector = new WinDetector();

        for (int i = 0; i < 11; i++) {
            board.placeStone(i, i, PlayerColor.WHITE);
            if (i < 10) {
                board.placeTile(i, i, PlayerColor.WHITE);
            }
        }

        assertEquals(PlayerColor.WHITE, detector.checkWinner(board));
    }

    @Test
    void doesNotDeclareWinnerWhenChainIsBroken() {
        Board board = new Board();
        WinDetector detector = new WinDetector();

        for (int row = 0; row < 11; row++) {
            if (row != 5) {
                board.placeStone(row, 0, PlayerColor.BLACK);
            }
        }

        assertNull(detector.checkWinner(board));
    }
}
