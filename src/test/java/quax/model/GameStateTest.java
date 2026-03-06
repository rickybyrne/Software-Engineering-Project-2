package quax.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void humanVsBotSetsWhitePlayerAsBot() {
        GameState state = new GameState(GameMode.HUMAN_V_BOT);

        Player whitePlayer = null;
        for (Player player : state.getPlayers()) {
            if (player.getColor() == PlayerColor.WHITE) {
                whitePlayer = player;
                break;
            }
        }

        assertNotNull(whitePlayer);
        assertEquals(PlayerKind.BOT, whitePlayer.getKind());
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
    void swapPlayerColoursSwapsPlayerColorsAndTurn() {
        GameState state = new GameState(GameMode.HUMAN_V_HUMAN);

        state.apply(new Move(CellType.OCT, 0, 0, PlayerColor.BLACK));

        Player blackBeforeSwap = findPlayerByColor(state, PlayerColor.BLACK);
        Player whiteBeforeSwap = findPlayerByColor(state, PlayerColor.WHITE);

        assertNotNull(blackBeforeSwap);
        assertNotNull(whiteBeforeSwap);

        state.swapPlayerColours();
        state.disablePieRule();

        Player blackAfterSwap = findPlayerByColor(state, PlayerColor.BLACK);
        Player whiteAfterSwap = findPlayerByColor(state, PlayerColor.WHITE);

        assertEquals(blackBeforeSwap, whiteAfterSwap);
        assertEquals(whiteBeforeSwap, blackAfterSwap);
        assertEquals(PlayerColor.WHITE, state.getBoard().getOct(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, state.getCurrentTurn());
        assertFalse(state.canUsePieRule());
        assertFalse(state.isPieRuleAvailable());
    }

    @Test
    void swapPlayerColoursRecoloursFirstRhombMove() {
        GameState state = new GameState(GameMode.HUMAN_V_HUMAN);

        state.apply(new Move(CellType.RHOMB, 0, 0, PlayerColor.BLACK));

        state.swapPlayerColours();
        state.disablePieRule();

        assertEquals(PlayerColor.WHITE, state.getBoard().getRhomb(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, state.getCurrentTurn());
        assertFalse(state.isPieRuleAvailable());
    }

    private Player findPlayerByColor(GameState state, PlayerColor color) {
        for (Player player : state.getPlayers()) {
            if (player.getColor() == color) {
                return player;
            }
        }
        return null;
    }
}
