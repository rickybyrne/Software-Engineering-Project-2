package quax.model;

public class Board {

    private final Cell[][] octCells = new Cell[11][11];
    private final Cell[][] rhombCells = new Cell[10][10];

    public Board() {
        for (int r = 0; r < octCells.length; r++) {
            for (int c = 0; c < octCells[r].length; c++) {
                octCells[r][c] = new Cell();
            }
        }

        for (int r = 0; r < rhombCells.length; r++) {
            for (int c = 0; c < rhombCells[r].length; c++) {
                rhombCells[r][c] = new Cell();
            }
        }
    }

    public Board(Board other) {
        this();

        for (int r = 0; r < octCells.length; r++) {
            for (int c = 0; c < octCells[r].length; c++) {
                octCells[r][c].setOccupant(other.getOct(r, c).getOccupant());
                octCells[r][c].setMoveOrder(other.getOct(r, c).getMoveOrder());
            }
        }

        for (int r = 0; r < rhombCells.length; r++) {
            for (int c = 0; c < rhombCells[r].length; c++) {
                rhombCells[r][c].setOccupant(other.getRhomb(r, c).getOccupant());
                rhombCells[r][c].setMoveOrder(other.getRhomb(r, c).getMoveOrder());
            }
        }
    }

    public Board copy() {
        return new Board(this);
    }

    public boolean isOctEmpty(int r, int c) {
        if (!isOctInBounds(r, c)) {
            return false;
        }
        return octCells[r][c].isEmpty();
    }

    public boolean isRhombEmpty(int r, int c) {
        if (!isRhombInBounds(r, c)) {
            return false;
        }
        return rhombCells[r][c].isEmpty();
    }

    public Cell getOct(int r, int c) {
        if (!isOctInBounds(r, c)) {
            throw new IllegalArgumentException("Oct coordinates are out of bounds.");
        }
        return octCells[r][c];
    }

    public Cell getRhomb(int r, int c) {
        if (!isRhombInBounds(r, c)) {
            throw new IllegalArgumentException("Rhomb coordinates are out of bounds.");
        }
        return rhombCells[r][c];
    }

    public void placeStone(int r, int c, PlayerColor color) {
        placeStone(r, c, color, 0);
    }

    public void placeStone(int r, int c, PlayerColor color, int moveOrder) {
        Cell cell = getOct(r, c);
        cell.setOccupant(color);
        cell.setMoveOrder(color == null ? 0 : moveOrder);
    }

    public void placeTile(int r, int c, PlayerColor color) {
        placeTile(r, c, color, 0);
    }

    public void placeTile(int r, int c, PlayerColor color, int moveOrder) {
        Cell cell = getRhomb(r, c);
        cell.setOccupant(color);
        cell.setMoveOrder(color == null ? 0 : moveOrder);
    }

    public boolean isOctInBounds(int r, int c) {
        return r >= 0 && r < 11 && c >= 0 && c < 11;
    }

    public boolean isRhombInBounds(int r, int c) {
        return r >= 0 && r < 10 && c >= 0 && c < 10;
    }



}
