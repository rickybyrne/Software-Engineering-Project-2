package quax.model;

public class BotEngine {

    private BotStrategy strategy;
    private boolean devMode;

    public BotEngine(BotStrategy strategy, boolean devMode) {
        this.strategy = strategy;
        this.devMode = devMode;
    }

    public Move chooseMove(GameState state) {
        if (strategy == null) {
            return null;
        }
        return strategy.selectMove(state);
    }

    public StrategyOverlay computeOverlay(GameState state) {
        if (!devMode || strategy == null) {
            return null;
        }
        return strategy.explain(state);
    }

    public BotStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(BotStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }
}
