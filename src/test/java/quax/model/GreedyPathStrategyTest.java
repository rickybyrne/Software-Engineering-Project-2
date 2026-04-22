package quax.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GreedyPathStrategyTest {

    private final GreedyPathStrategy strategy = new GreedyPathStrategy();
    private final MoveValidator validator = new MoveValidator();

    @Test
    void selectMoveReturnsLegalMoveForCurrentPlayer() {
        GameState state = new GameState(GameMode.HUMAN_V_BOT);

        Move move = strategy.selectMove(state);

        assertNotNull(move);
        assertEquals(PlayerColor.BLACK, move.getPlayer());
        assertTrue(validator.validate(move, state));
    }

    @Test
    void selectsImmediateWinningMove() {
        GameState state = blackTurnAfterTwoMoves();

        for (int row = 0; row < 10; row++) {
            state.getBoard().placeStone(row, 0, PlayerColor.BLACK);
        }

        Move move = strategy.selectMove(state);

        assertMove(move, CellType.OCT, 10, 0, PlayerColor.BLACK);
    }

    @Test
    void blocksImmediateWhiteWin() {
        GameState state = blackTurnAfterTwoMoves();

        for (int col = 0; col < 10; col++) {
            state.getBoard().placeStone(0, col, PlayerColor.WHITE);
        }

        Move move = strategy.selectMove(state);

        assertMove(move, CellType.OCT, 0, 10, PlayerColor.BLACK);
    }

    @Test
    void strategyIsDeterministicForSameState() {
        GameState state = blackTurnAfterTwoMoves();
        state.getBoard().placeStone(5, 4, PlayerColor.BLACK);
        state.getBoard().placeStone(0, 0, PlayerColor.WHITE);

        Move first = strategy.selectMove(state);
        Move second = strategy.selectMove(state);

        assertMove(second, first.getCellType(), first.getR(), first.getC(), first.getPlayer());
    }

    @Test
    void selectMoveAndExplainDoNotMutateBoard() {
        GameState state = blackTurnAfterTwoMoves();
        String before = snapshot(state.getBoard());

        strategy.selectMove(state);
        strategy.explain(state);

        assertEquals(before, snapshot(state.getBoard()));
    }

    @Test
    void explainReturnsWeightsForAllLegalMovesAndSuggestedPath() {
        GameState state = blackTurnAfterTwoMoves();

        StrategyOverlay overlay = strategy.explain(state);
        Move selectedMove = strategy.selectMove(state);

        assertNotNull(overlay);
        assertEquals(countLegalMoves(state), overlay.getWeights().size());
        assertFalse(overlay.getSuggestedPath().isEmpty());
        assertTrue(overlay.getWeights().containsKey(idFor(selectedMove)));
    }

    private GameState blackTurnAfterTwoMoves() {
        GameState state = new GameState(GameMode.HUMAN_V_HUMAN);
        state.apply(new Move(CellType.OCT, 10, 10, PlayerColor.BLACK));
        state.apply(new Move(CellType.OCT, 10, 9, PlayerColor.WHITE));
        return state;
    }

    private int countLegalMoves(GameState state) {
        int count = 0;
        Board board = state.getBoard();

        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 11; col++) {
                if (board.isOctEmpty(row, col)) {
                    count++;
                }
            }
        }

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (board.isRhombEmpty(row, col)) {
                    count++;
                }
            }
        }

        return count;
    }

    private String snapshot(Board board) {
        StringBuilder snapshot = new StringBuilder();

        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 11; col++) {
                snapshot.append(board.getOct(row, col).getOccupant()).append('|');
            }
        }

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                snapshot.append(board.getRhomb(row, col).getOccupant()).append('|');
            }
        }

        return snapshot.toString();
    }

    private String idFor(Move move) {
        return move.getCellType() + ":" + move.getR() + ":" + move.getC();
    }

    private void assertMove(Move move, CellType cellType, int row, int col, PlayerColor player) {
        assertNotNull(move);
        assertEquals(cellType, move.getCellType());
        assertEquals(row, move.getR());
        assertEquals(col, move.getC());
        assertEquals(player, move.getPlayer());
    }
}
