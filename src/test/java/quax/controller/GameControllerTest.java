package quax.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import quax.model.CellType;
import quax.model.GameMode;
import quax.model.GameState;
import quax.model.Move;
import quax.model.PlayerColor;
import quax.model.StrategyOverlay;

class GameControllerTest {

    @Test
    void newHumanVsBotGameAppliesBotOpeningMove() {
        GameController controller = new GameController();

        controller.newGame(GameMode.HUMAN_V_BOT);

        GameState state = controller.getState();
        Move openingMove = findOnlyOccupiedCell(state, PlayerColor.BLACK);

        assertNotNull(state);
        assertEquals(GameMode.HUMAN_V_BOT, state.getMode());
        assertEquals(PlayerColor.BLACK, occupantAt(state, openingMove));
        assertEquals(PlayerColor.WHITE, state.getCurrentTurn());
        assertEquals(1, state.getMoveCount());
        assertEquals(1, countOccupants(state, PlayerColor.BLACK));
        assertEquals(0, countOccupants(state, PlayerColor.WHITE));
        assertTrue(controller.canActivatePieRule());
    }

    @Test
    void humanMoveInHumanVsBotTriggersSingleBotReply() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_BOT);

        assertTrue(controller.handleOctClick(0, 0));

        GameState state = controller.getState();
        assertEquals(PlayerColor.WHITE, state.getBoard().getOct(0, 0).getOccupant());
        assertEquals(PlayerColor.WHITE, state.getCurrentTurn());
        assertEquals(3, state.getMoveCount());
        assertEquals(2, countOccupants(state, PlayerColor.BLACK));
        assertFalse(controller.canActivatePieRule());
    }

    @Test
    void pieRuleInHumanVsBotRecoloursOpeningMoveAndBotPlacesAgain() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_BOT);
        Move openingMove = findOnlyOccupiedCell(controller.getState(), PlayerColor.BLACK);

        assertTrue(controller.activatePieRule());

        GameState state = controller.getState();
        assertEquals(PlayerColor.WHITE, occupantAt(state, openingMove));
        assertEquals(PlayerColor.WHITE, state.getCurrentTurn());
        assertEquals(2, state.getMoveCount());
        assertEquals(1, countOccupants(state, PlayerColor.BLACK));
        assertFalse(controller.canActivatePieRule());
    }

    @Test
    void humanVsBotGameCachesOverlayForLastBotMove() {
        GameController controller = new GameController();

        controller.newGame(GameMode.HUMAN_V_BOT);

        StrategyOverlay overlay = controller.getLastBotOverlay();

        assertNotNull(overlay);
        assertFalse(overlay.getWeights().isEmpty());
        assertFalse(overlay.getSuggestedPath().isEmpty());
    }

    @Test
    void handleOctClickPlacesBlackStoneAndChangesTurn() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        boolean moveAccepted = controller.handleOctClick(0, 0);

        assertTrue(moveAccepted);
        assertEquals(PlayerColor.BLACK, controller.getState().getBoard().getOct(0, 0).getOccupant());
        assertEquals(PlayerColor.WHITE, controller.getState().getCurrentTurn());
    }

    @Test
    void handleRhombClickPlacesCurrentPlayerTileAndChangesTurn() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        boolean moveAccepted = controller.handleRhombClick(0, 0);

        assertTrue(moveAccepted);
        assertEquals(PlayerColor.BLACK, controller.getState().getBoard().getRhomb(0, 0).getOccupant());
        assertEquals(PlayerColor.WHITE, controller.getState().getCurrentTurn());
    }

    @Test
    void secondMoveOnSameOctCellIsRejected() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        assertTrue(controller.handleOctClick(0, 0));

        boolean secondMoveAccepted = controller.handleOctClick(0, 0);

        assertFalse(secondMoveAccepted);
        assertEquals(PlayerColor.BLACK, controller.getState().getBoard().getOct(0, 0).getOccupant());
    }

    @Test
    void secondMoveOnSameRhombCellIsRejected() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        assertTrue(controller.handleRhombClick(0, 0));

        boolean secondMoveAccepted = controller.handleRhombClick(0, 0);

        assertFalse(secondMoveAccepted);
        assertEquals(PlayerColor.BLACK, controller.getState().getBoard().getRhomb(0, 0).getOccupant());
    }

    @Test
    void pieRuleCanActivateOnlyAfterFirstBlackMoveAndOnlyOnce() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        assertFalse(controller.canActivatePieRule());
        assertFalse(controller.activatePieRule());

        assertTrue(controller.handleOctClick(0, 0));
        assertTrue(controller.canActivatePieRule());
        assertTrue(controller.activatePieRule());

        assertEquals(PlayerColor.WHITE, controller.getState().getBoard().getOct(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, controller.getState().getCurrentTurn());
        assertFalse(controller.canActivatePieRule());
        assertFalse(controller.activatePieRule());
    }

    @Test
    void pieRuleRecoloursFirstRhombOpeningMove() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        assertTrue(controller.handleRhombClick(0, 0));
        assertTrue(controller.canActivatePieRule());

        assertTrue(controller.activatePieRule());

        assertEquals(PlayerColor.WHITE, controller.getState().getBoard().getRhomb(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, controller.getState().getCurrentTurn());
    }

    @Test
    void blackWinsWithConnectedTopToBottomStoneChain() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        assertTrue(controller.handleOctClick(0, 0));
        assertTrue(controller.handleOctClick(0, 1));
        assertTrue(controller.handleOctClick(1, 0));
        assertTrue(controller.handleOctClick(1, 1));
        assertTrue(controller.handleOctClick(2, 0));
        assertTrue(controller.handleOctClick(2, 1));
        assertTrue(controller.handleOctClick(3, 0));
        assertTrue(controller.handleOctClick(3, 1));
        assertTrue(controller.handleOctClick(4, 0));
        assertTrue(controller.handleOctClick(4, 1));
        assertTrue(controller.handleOctClick(5, 0));
        assertTrue(controller.handleOctClick(5, 1));
        assertTrue(controller.handleOctClick(6, 0));
        assertTrue(controller.handleOctClick(6, 1));
        assertTrue(controller.handleOctClick(7, 0));
        assertTrue(controller.handleOctClick(7, 1));
        assertTrue(controller.handleOctClick(8, 0));
        assertTrue(controller.handleOctClick(8, 1));
        assertTrue(controller.handleOctClick(9, 0));
        assertTrue(controller.handleOctClick(9, 1));
        assertTrue(controller.handleOctClick(10, 0));

        GameState state = controller.getState();
        assertTrue(state.isGameOver());
        assertEquals(PlayerColor.BLACK, state.getWinner());
    }

    @Test
    void movesAreRejectedAfterWinnerIsDetected() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        for (int row = 0; row < 10; row++) {
            assertTrue(controller.handleOctClick(row, 0));
            assertTrue(controller.handleOctClick(row, 1));
        }
        assertTrue(controller.handleOctClick(10, 0));

        GameState state = controller.getState();
        assertTrue(state.isGameOver());
        assertEquals(PlayerColor.BLACK, state.getWinner());

        boolean moveAcceptedAfterWin = controller.handleOctClick(10, 1);

        assertFalse(moveAcceptedAfterWin);
        assertNull(state.getBoard().getOct(10, 1).getOccupant());
    }

    @Test
    void devModeRightClickCyclesOctTileWithoutChangingTurn() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        assertFalse(controller.handleOctRightClick(0, 0));
        assertNull(controller.getState().getBoard().getOct(0, 0).getOccupant());

        assertTrue(controller.toggleDevMode());

        assertTrue(controller.handleOctRightClick(0, 0));
        assertEquals(PlayerColor.WHITE, controller.getState().getBoard().getOct(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, controller.getState().getCurrentTurn());
        assertEquals(0, controller.getState().getMoveCount());

        assertTrue(controller.handleOctRightClick(0, 0));
        assertEquals(PlayerColor.BLACK, controller.getState().getBoard().getOct(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, controller.getState().getCurrentTurn());
        assertEquals(0, controller.getState().getMoveCount());

        assertTrue(controller.handleOctRightClick(0, 0));
        assertNull(controller.getState().getBoard().getOct(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, controller.getState().getCurrentTurn());
        assertEquals(0, controller.getState().getMoveCount());
    }

    @Test
    void devModeRightClickCyclesRhombTileWithoutChangingTurn() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        assertTrue(controller.toggleDevMode());

        assertTrue(controller.handleRhombRightClick(0, 0));
        assertEquals(PlayerColor.WHITE, controller.getState().getBoard().getRhomb(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, controller.getState().getCurrentTurn());
        assertEquals(0, controller.getState().getMoveCount());

        assertTrue(controller.handleRhombRightClick(0, 0));
        assertEquals(PlayerColor.BLACK, controller.getState().getBoard().getRhomb(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, controller.getState().getCurrentTurn());
        assertEquals(0, controller.getState().getMoveCount());

        assertTrue(controller.handleRhombRightClick(0, 0));
        assertNull(controller.getState().getBoard().getRhomb(0, 0).getOccupant());
        assertEquals(PlayerColor.BLACK, controller.getState().getCurrentTurn());
        assertEquals(0, controller.getState().getMoveCount());
    }

    @Test
    void newGameResetsDevModeToDisabled() {
        GameController controller = new GameController();
        controller.newGame(GameMode.HUMAN_V_HUMAN);

        assertTrue(controller.toggleDevMode());
        assertTrue(controller.isDevModeEnabled());

        controller.newGame(GameMode.HUMAN_V_BOT);

        assertFalse(controller.isDevModeEnabled());
        assertFalse(controller.handleOctRightClick(0, 0));
    }

    private int countOccupants(GameState state, PlayerColor color) {
        int count = 0;

        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 11; col++) {
                if (state.getBoard().getOct(row, col).getOccupant() == color) {
                    count++;
                }
            }
        }

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (state.getBoard().getRhomb(row, col).getOccupant() == color) {
                    count++;
                }
            }
        }

        return count;
    }

    private Move findOnlyOccupiedCell(GameState state, PlayerColor color) {
        Move found = null;

        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 11; col++) {
                if (state.getBoard().getOct(row, col).getOccupant() == color) {
                    assertNull(found);
                    found = new Move(CellType.OCT, row, col, color);
                }
            }
        }

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (state.getBoard().getRhomb(row, col).getOccupant() == color) {
                    assertNull(found);
                    found = new Move(CellType.RHOMB, row, col, color);
                }
            }
        }

        assertNotNull(found);
        return found;
    }

    private PlayerColor occupantAt(GameState state, Move move) {
        if (move.getCellType() == CellType.OCT) {
            return state.getBoard().getOct(move.getR(), move.getC()).getOccupant();
        }

        return state.getBoard().getRhomb(move.getR(), move.getC()).getOccupant();
    }
}
