package quax.controller;

import quax.model.PlayerColor;

/**
 * Encapsulates DevMode state and tile-cycling behaviour.
 */
public class DevModeEditor {

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public void disable() {
        enabled = false;
    }

    /**
     * Cycle order: UNOCCUPIED -> WHITE -> BLACK -> UNOCCUPIED.
     */
    public PlayerColor nextOccupant(PlayerColor currentOccupant) {
        if (currentOccupant == PlayerColor.WHITE) {
            return PlayerColor.BLACK;
        }

        if (currentOccupant == PlayerColor.BLACK) {
            return null;
        }

        return PlayerColor.WHITE;
    }
}
