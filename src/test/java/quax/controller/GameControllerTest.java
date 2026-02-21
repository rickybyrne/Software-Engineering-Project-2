package quax.controller;

import org.junit.jupiter.api.Test;
import quax.model.GameMode;
import quax.model.GameState;
import quax.model.PlayerColor;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
