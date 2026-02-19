package quax.model;

import java.util.List;
import java.util.Map;

public class StrategyOverlay {

    private final Map<String, Double> weights;
    private final List<String> notes;

    public StrategyOverlay(Map<String, Double> weights, List<String> notes) {
        this.weights = weights;
        this.notes = notes;
    }

    public Map<String, Double> getWeights() {
        return weights;
    }

    public List<String> getNotes() {
        return notes;
    }
}
