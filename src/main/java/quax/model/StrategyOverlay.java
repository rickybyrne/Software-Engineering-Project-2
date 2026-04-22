package quax.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StrategyOverlay {

    private final Map<String, Double> weights;
    private final List<String> notes;
    private final List<String> suggestedPath;

    public StrategyOverlay(Map<String, Double> weights, List<String> notes) {
        this(weights, notes, Collections.emptyList());
    }

    public StrategyOverlay(Map<String, Double> weights, List<String> notes, List<String> suggestedPath) {
        this.weights = weights;
        this.notes = notes;
        this.suggestedPath = suggestedPath == null ? Collections.emptyList() : suggestedPath;
    }

    public Map<String, Double> getWeights() {
        return weights;
    }

    public List<String> getNotes() {
        return notes;
    }

    public List<String> getSuggestedPath() {
        return suggestedPath;
    }
}
