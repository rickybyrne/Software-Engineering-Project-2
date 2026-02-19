package quax.model;

public class Cell {

    private PlayerColor occupant;

    public boolean isEmpty() {
        return occupant == null;
    }

    public PlayerColor getOccupant() {
        return occupant;
    }

    public void setOccupant(PlayerColor color) {
        this.occupant = color;
    }
}
