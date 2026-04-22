package quax.controller;

import quax.model.BotEngine;
import quax.model.CellType;
import quax.model.GameMode;
import quax.model.GameState;
import quax.model.GreedyPathStrategy;
import quax.model.Move;
import quax.model.MoveValidator;
import quax.model.PlayerColor;
import quax.model.StrategyOverlay;
import quax.model.WinDetector;

public class GameController {

    private GameState state;
    private final MoveValidator validator;
    private final WinDetector winDetector;
    private final DevModeEditor devModeEditor;
    private BotEngine botEngine;
    private StrategyOverlay lastBotOverlay;

    public GameController() {
        this.validator = new MoveValidator();
        this.winDetector = new WinDetector();
        this.devModeEditor = new DevModeEditor();
    }

    public boolean canActivatePieRule() {
        return state != null && state.canUsePieRule();
    }

    public boolean activatePieRule() {
        if (!canActivatePieRule()) {
            return false;
        }

        state.claimOpeningMoveForWhite();
        applyBotMoveIfNeeded();
        return true;
    }

    public void newGame(GameMode mode) {
        state = new GameState(mode);
        devModeEditor.disable();
        lastBotOverlay = null;

        if (mode == GameMode.HUMAN_V_BOT) {
            botEngine = new BotEngine(new GreedyPathStrategy(), false);
            applyBotMoveIfNeeded();
        } else {
            botEngine = null;
        }
    }

    public boolean restartGame() {
        if (state == null) {
            return false;
        }

        newGame(state.getMode());
        return true;
    }

    public boolean handleOctClick(int r, int c) {
        if (state == null) {
            return false;
        }

        if (state.isCurrentTurnBot()) {
            return false;
        }

        Move move = new Move(CellType.OCT, r, c, state.getCurrentTurn());
        return handleMove(move);
    }

    public boolean handleRhombClick(int r, int c) {
        if (state == null) {
            return false;
        }

        if (state.isCurrentTurnBot()) {
            return false;
        }

        Move move = new Move(CellType.RHOMB, r, c, state.getCurrentTurn());
        return handleMove(move);
    }

    public boolean handleOctRightClick(int r, int c) {
        return handleDevEdit(CellType.OCT, r, c);
    }

    public boolean handleRhombRightClick(int r, int c) {
        return handleDevEdit(CellType.RHOMB, r, c);
    }

    private boolean handleDevEdit(CellType cellType, int r, int c) {
        if (state == null || !devModeEditor.isEnabled()) {
            return false;
        }

        if (cellType == CellType.OCT) {
            if (!state.getBoard().isOctInBounds(r, c)) {
                return false;
            }

            PlayerColor current = state.getBoard().getOct(r, c).getOccupant();
            PlayerColor next = devModeEditor.nextOccupant(current);
            state.getBoard().placeStone(r, c, next);
            return true;
        }

        if (cellType == CellType.RHOMB) {
            if (!state.getBoard().isRhombInBounds(r, c)) {
                return false;
            }

            PlayerColor current = state.getBoard().getRhomb(r, c).getOccupant();
            PlayerColor next = devModeEditor.nextOccupant(current);
            state.getBoard().placeTile(r, c, next);
            return true;
        }

        return false;
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

        applyBotMoveIfNeeded();

        return true;
    }

    private boolean applyBotMoveIfNeeded() {
        if (state == null || state.isGameOver() || botEngine == null || !state.isCurrentTurnBot()) {
            return false;
        }

        if (botEngine.getStrategy() != null) {
            lastBotOverlay = botEngine.getStrategy().explain(state);
        }

        Move botMove = botEngine.chooseMove(state);
        if (!validator.validate(botMove, state)) {
            lastBotOverlay = null;
            return false;
        }

        state.apply(botMove);

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

    public StrategyOverlay getLastBotOverlay() {
        return lastBotOverlay;
    }

    public GameState getState() {
        return state;
    }

    public boolean toggleDevMode() {
        return devModeEditor.toggle();
    }

    public boolean isDevModeEnabled() {
        return devModeEditor.isEnabled();
    }
}
