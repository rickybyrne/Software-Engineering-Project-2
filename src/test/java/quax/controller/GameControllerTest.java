package quax.controller;

import org.junit.jupiter.api.Test;
import quax.model.GameMode;
import quax.model.GameState;
import quax.model.PlayerColor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControllerTest {

    @Test
    void newGameUsesSelectedMode() {
        GameController controller = new GameController();

        controller.newGame(GameMode.HUMAN_V_BOT);

        GameState state = controller.getState();
        assertNotNull(state);
        assertEquals(GameMode.HUMAN_V_BOT, state.getMode());
        assertEquals(PlayerColor.BLACK, state.getCurrentTurn());
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
}
