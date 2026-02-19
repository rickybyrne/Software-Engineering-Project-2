package quax.model;

public class Move {

    private final CellType cellType;
    private final int r;
    private final int c;
    private final PlayerColor player;

    public Move(CellType cellType, int r, int c, PlayerColor player) {
        this.cellType = cellType;
        this.r = r;
        this.c = c;
        this.player = player;
    }

    public boolean isStoneMove() {
        return cellType == CellType.OCT;
    }

    public boolean isTileMove() {
        return cellType == CellType.RHOMB;
    }

    public CellType getCellType() {
        return cellType;
    }

    public int getR() {
        return r;
    }

    public int getC() {
        return c;
    }

    public PlayerColor getPlayer() {
        return player;
    }
}
