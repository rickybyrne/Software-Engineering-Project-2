package quax.model;

public interface BotStrategy {

    Move selectMove(GameState state);

    StrategyOverlay explain(GameState state);
}
