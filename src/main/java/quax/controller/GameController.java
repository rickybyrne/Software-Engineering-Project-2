package quax.controller;

import quax.model.BotEngine;
import quax.model.CellType;
import quax.model.GameMode;
import quax.model.GameState;
import quax.model.Move;
import quax.model.MoveValidator;
import quax.model.PlayerColor;
import quax.model.WinDetector;

public class GameController {

    private GameState state;
    private final MoveValidator validator;
    private final WinDetector winDetector;
    private BotEngine botEngine;

    public GameController() {
        this.validator = new MoveValidator();
        this.winDetector = new WinDetector();
    }

    public boolean canActivatePieRule() {
        return state != null && state.canUsePieRule();
    }

    public boolean activatePieRule() {
        if (!canActivatePieRule()) {
            return false;
        }

        state.swapPlayerColours();
        state.disablePieRule();
        return true;
    }

    public void newGame(GameMode mode) {
        state = new GameState(mode);

        if (mode == GameMode.HUMAN_V_BOT) {
            botEngine = new BotEngine(null, false);
        } else {
            botEngine = null;
        }
    }

    public boolean handleOctClick(int r, int c) {
        if (state == null) {
            return false;
        }

        Move move = new Move(CellType.OCT, r, c, state.getCurrentTurn());
        return handleMove(move);
    }

    public boolean handleRhombClick(int r, int c) {
        if (state == null) {
            return false;
        }

        Move move = new Move(CellType.RHOMB, r, c, state.getCurrentTurn());
        return handleMove(move);
    }

    private boolean handleMove(Move move) {
        if (state == null || state.isGameOver()) {
            return false;
        }

        if (!validator.validate(move, state)) {
            return false;
        }

        state.apply(move);

        PlayerColor winner = winDetector.checkWinner(state.getBoard());
        if (winner != null) {
            state.endGame(winner);
        }

        return true;
    }

    public void toggleStrategyOverlay(boolean show) {
        if (botEngine == null) {
            return;
        }

        botEngine.setDevMode(show);
    }

    public GameState getState() {
        return state;
    }
}
