package quax.model;

public class Cell {

    private PlayerColor occupant;
    private int moveOrder;

    public boolean isEmpty() {
        return occupant == null;
    }

    public PlayerColor getOccupant() {
        return occupant;
    }

    public void setOccupant(PlayerColor color) {
        this.occupant = color;
    }

    public int getMoveOrder() {
        return moveOrder;
    }

    public void setMoveOrder(int moveOrder) {
        this.moveOrder = moveOrder;
    }
}
