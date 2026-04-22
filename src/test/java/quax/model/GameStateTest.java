package quax.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

    @Test
    void startsWithBlackTurnInHumanVsHuman() {
        GameState state = new GameState(GameMode.HUMAN_V_HUMAN);

        assertEquals(PlayerColor.BLACK, state.getCurrentTurn());
    }

    @Test
    void startsWithBlackTurnInHumanVsBot() {
        GameState state = new GameState(GameMode.HUMAN_V_BOT);

        assertEquals(PlayerColor.BLACK, state.getCurrentTurn());
    }

    @Test
    void humanVsBotSetsBlackPlayerAsBotAndWhitePlayerAsHuman() {
        GameState state = new GameState(GameMode.HUMAN_V_BOT);

        Player blackPlayer = state.getPlayerForColor(PlayerColor.BLACK);
        Player whitePlayer = state.getPlayerForColor(PlayerColor.WHITE);

        assertNotNull(blackPlayer);
        assertNotNull(whitePlayer);
        assertEquals(PlayerKind.BOT, blackPlayer.getKind());
        assertEquals(PlayerKind.HUMAN, whitePlayer.getKind());
        assertTrue(state.isCurrentTurnBot());
    }

    @Test
    void pieRuleBecomesAvailableAfterFirstMoveForWhiteTurn() {
        GameState state = new GameState(GameMode.HUMAN_V_HUMAN);

        assertFalse(state.canUsePieRule());

        Move firstMove = new Move(CellType.OCT, 0, 0, PlayerColor.BLACK);
        state.apply(firstMove);

        assertEquals(PlayerColor.WHITE, state.getCurrentTurn());
        assertTrue(state.canUsePieRule());
    }

    @Test
    void applyRecordsMoveOrderOnPlacedCells() {
        GameState state = new GameState(GameMode.HUMAN_V_HUMAN);

        state.apply(new Move(CellType.OCT, 0, 0, PlayerColor.BLACK));
        state.apply(new Move(CellType.RHOMB, 0, 0, PlayerColor.WHITE));

        assertEquals(1, state.getBoard().getOct(0, 0).getMoveOrder());
        assertEquals(2, state.getBoard().getRhomb(0, 0).getMoveOrder());
    }

    @Test
    void claimOpeningMoveForWhiteRecoloursOpeningOctWithoutSwappingPlayers() {
        GameState state = new GameState(GameMode.HUMAN_V_HUMAN);

        state.apply(new Move(CellType.OCT, 0, 0, PlayerColor.BLACK));

        Player blackPlayer = state.getPlayerForColor(PlayerColor.BLACK);
        Player whitePlayer = state.getPlayerForColor(PlayerColor.WHITE);

        assertNotNull(blackPlayer);
        assertNotNull(whitePlayer);

        state.claimOpeningMoveForWhite();

        assertSame(blackPlayer, state.getPlayerForColor(PlayerColor.BLACK));
        assertSame(whitePlayer, state.getPlayerForColor(PlayerColor.WHITE));
        assertEquals(PlayerColor.WHITE, state.getBoard().getOct(0, 0).getOccupant());
        assertEquals(1, state.getBoard().getOct(0, 0).getMoveOrder());
        assertEquals(PlayerColor.BLACK, state.getCurrentTurn());
        assertFalse(state.canUsePieRule());
        assertFalse(state.isPieRuleAvailable());
    }

    @Test
    void claimOpeningMoveForWhiteRecoloursOpeningRhombWithoutSwappingPlayers() {
        GameState state = new GameState(GameMode.HUMAN_V_HUMAN);

        state.apply(new Move(CellType.RHOMB, 0, 0, PlayerColor.BLACK));

        state.claimOpeningMoveForWhite();

        assertEquals(PlayerColor.WHITE, state.getBoard().getRhomb(0, 0).getOccupant());
        assertEquals(1, state.getBoard().getRhomb(0, 0).getMoveOrder());
        assertEquals(PlayerColor.BLACK, state.getCurrentTurn());
        assertFalse(state.isPieRuleAvailable());
    }
}
