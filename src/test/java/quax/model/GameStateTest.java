package quax.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
