package quax.model;

import java.util.ArrayDeque;
import java.util.Deque;

public class WinDetector {

    public PlayerColor checkWinner(Board board) {
        PlayerColor blackWinner = findWinner(board, PlayerColor.BLACK);
        if (blackWinner != null) {
            return blackWinner;
        }

        return findWinner(board, PlayerColor.WHITE);
    }

    private PlayerColor findWinner(Board board, PlayerColor color) {
        boolean[][] visited = new boolean[11][11];
        Deque<int[]> frontier = new ArrayDeque<>();

        if (color == PlayerColor.BLACK) {
            for (int c = 0; c < 11; c++) {
                if (board.getOct(0, c).getOccupant() == color) {
                    frontier.add(new int[]{0, c});
                    visited[0][c] = true;
                }
            }
        } else {
            for (int r = 0; r < 11; r++) {
                if (board.getOct(r, 0).getOccupant() == color) {
                    frontier.add(new int[]{r, 0});
                    visited[r][0] = true;
                }
            }
        }

        while (!frontier.isEmpty()) {
            int[] current = frontier.removeFirst();
            int row = current[0];
            int col = current[1];

            if (reachesGoalSide(color, row, col)) {
                return color;
            }

            addOrthogonalNeighbours(board, color, visited, frontier, row, col);
            addDiagonalNeighbours(board, color, visited, frontier, row, col);
        }

        return null;
    }

    private boolean reachesGoalSide(PlayerColor color, int row, int col) {
        if (color == PlayerColor.BLACK) {
            return row == 10;
        }

        return col == 10;
    }

    private void addOrthogonalNeighbours(
            Board board,
            PlayerColor color,
            boolean[][] visited,
            Deque<int[]> frontier,
            int row,
            int col
    ) {
        int[][] directions = {
                {-1, 0},
                {1, 0},
                {0, -1},
                {0, 1}
        };

        for (int[] direction : directions) {
            enqueueIfOwned(board, color, visited, frontier, row + direction[0], col + direction[1]);
        }
    }

    private void addDiagonalNeighbours(
            Board board,
            PlayerColor color,
            boolean[][] visited,
            Deque<int[]> frontier,
            int row,
            int col
    ) {
        addDiagonalNeighbour(board, color, visited, frontier, row - 1, col - 1, row - 1, col - 1);
        addDiagonalNeighbour(board, color, visited, frontier, row - 1, col + 1, row - 1, col);
        addDiagonalNeighbour(board, color, visited, frontier, row + 1, col - 1, row, col - 1);
        addDiagonalNeighbour(board, color, visited, frontier, row + 1, col + 1, row, col);
    }

    private void addDiagonalNeighbour(
            Board board,
            PlayerColor color,
            boolean[][] visited,
            Deque<int[]> frontier,
            int targetRow,
            int targetCol,
            int rhombRow,
            int rhombCol
    ) {
        if (!board.isOctInBounds(targetRow, targetCol) || !board.isRhombInBounds(rhombRow, rhombCol)) {
            return;
        }

        if (board.getRhomb(rhombRow, rhombCol).getOccupant() != color) {
            return;
        }

        enqueueIfOwned(board, color, visited, frontier, targetRow, targetCol);
    }

    private void enqueueIfOwned(
            Board board,
            PlayerColor color,
            boolean[][] visited,
            Deque<int[]> frontier,
            int row,
            int col
    ) {
        if (!board.isOctInBounds(row, col) || visited[row][col]) {
            return;
        }

        if (board.getOct(row, col).getOccupant() != color) {
            return;
        }

        visited[row][col] = true;
        frontier.addLast(new int[]{row, col});
    }
}
