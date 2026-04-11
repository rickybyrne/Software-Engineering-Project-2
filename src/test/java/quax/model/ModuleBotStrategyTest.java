package quax.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ModuleBotStrategyTest {

    private final ModuleBotStrategy strategy = new ModuleBotStrategy();

    @Test
    void openingMoveIsOneRightOfCentre() {
        GameState state = new GameState(GameMode.HUMAN_V_BOT);

        Move move = strategy.selectMove(state);

        assertMove(move, CellType.OCT, 5, 6, PlayerColor.BLACK);
    }

    @Test
    void openingFallbackDoesNotChooseOccupiedCell() {
        GameState state = new GameState(GameMode.HUMAN_V_BOT);
        state.getBoard().placeStone(5, 6, PlayerColor.WHITE);

        Move move = strategy.selectMove(state);

        assertNotNull(move);
        assertFalse(move.getCellType() == CellType.OCT && move.getR() == 5 && move.getC() == 6);
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
    void prefersModuleConnectionOverUnrelatedMove() {
        GameState state = blackTurnAfterTwoMoves();
        state.getBoard().placeStone(5, 4, PlayerColor.BLACK);
        state.getBoard().placeStone(5, 6, PlayerColor.BLACK);

        Move move = strategy.selectMove(state);

        assertMove(move, CellType.OCT, 5, 5, PlayerColor.BLACK);
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

    private GameState blackTurnAfterTwoMoves() {
        GameState state = new GameState(GameMode.HUMAN_V_HUMAN);
        state.apply(new Move(CellType.OCT, 10, 10, PlayerColor.BLACK));
        state.apply(new Move(CellType.OCT, 10, 9, PlayerColor.WHITE));
        return state;
    }

    private void assertMove(Move move, CellType cellType, int row, int col, PlayerColor player) {
        assertNotNull(move);
        assertEquals(cellType, move.getCellType());
        assertEquals(row, move.getR());
        assertEquals(col, move.getC());
        assertEquals(player, move.getPlayer());
    }
}
