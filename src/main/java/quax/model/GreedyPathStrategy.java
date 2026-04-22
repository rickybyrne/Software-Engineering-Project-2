package quax.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class GreedyPathStrategy implements BotStrategy {

    private static final int BOARD_SIZE = 11;
    private static final int RHOMB_SIZE = 10;
    private static final int BLOCKED = 1_000_000;

    private final MoveValidator validator = new MoveValidator();

    @Override
    public Move selectMove(GameState state) {
        Evaluation evaluation = evaluate(state);
        return evaluation == null ? null : evaluation.best.move;
    }

    @Override
    public StrategyOverlay explain(GameState state) {
        Evaluation evaluation = evaluate(state);
        Map<String, Double> weights = new LinkedHashMap<>();
        List<String> notes = new ArrayList<>();

        if (evaluation == null) {
            notes.add("No legal bot move is available.");
            return new StrategyOverlay(weights, notes, Collections.emptyList());
        }

        for (CandidateScore candidate : evaluation.rankedCandidates) {
            weights.put(idFor(candidate.move), candidate.score);
        }

        notes.add("Selected " + idFor(evaluation.best.move) + " with score " + evaluation.best.score + ".");
        notes.add(
                "Path lengths after move: "
                        + evaluation.best.move.getPlayer()
                        + "="
                        + formatDistance(evaluation.best.ownDistance)
                        + ", "
                        + opponent(evaluation.best.move.getPlayer())
                        + "="
                        + formatDistance(evaluation.best.opponentDistance)
                        + "."
        );

        return new StrategyOverlay(weights, notes, new ArrayList<>(evaluation.best.pathIds));
    }

    private Evaluation evaluate(GameState state) {
        if (state == null || state.isGameOver()) {
            return null;
        }

        PlayerColor player = state.getCurrentTurn();
        List<Move> legalMoves = legalMoves(state, player);
        if (legalMoves.isEmpty()) {
            return null;
        }

        List<CandidateScore> rankedCandidates = new ArrayList<>();
        for (Move move : legalMoves) {
            Board copy = state.getBoard().copy();
            apply(copy, move);

            PathSearchResult ownPath = shortestPath(copy, player);
            int opponentDistance = shortestPath(copy, opponent(player)).cost;
            double score = opponentDistance - ownPath.cost;

            rankedCandidates.add(new CandidateScore(
                    move,
                    score,
                    ownPath.cost,
                    opponentDistance,
                    centerDistance(move),
                    ownPath.pathIds
            ));
        }

        rankedCandidates.sort(CandidateScore.BEST_FIRST);
        return new Evaluation(rankedCandidates.get(0), rankedCandidates);
    }

    private List<Move> legalMoves(GameState state, PlayerColor player) {
        Board board = state.getBoard();
        List<Move> moves = new ArrayList<>();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (!board.isOctEmpty(row, col)) {
                    continue;
                }

                Move move = new Move(CellType.OCT, row, col, player);
                if (validator.validate(move, state)) {
                    moves.add(move);
                }
            }
        }

        for (int row = 0; row < RHOMB_SIZE; row++) {
            for (int col = 0; col < RHOMB_SIZE; col++) {
                if (!board.isRhombEmpty(row, col)) {
                    continue;
                }

                Move move = new Move(CellType.RHOMB, row, col, player);
                if (validator.validate(move, state)) {
                    moves.add(move);
                }
            }
        }

        return moves;
    }

    private PathSearchResult shortestPath(Board board, PlayerColor color) {
        int[][] distances = new int[BOARD_SIZE][BOARD_SIZE];
        PathStep[][] previous = new PathStep[BOARD_SIZE][BOARD_SIZE];

        for (int[] row : distances) {
            Arrays.fill(row, BLOCKED);
        }

        PriorityQueue<PathNode> frontier = new PriorityQueue<>(PathNode.BEST_FIRST);

        if (color == PlayerColor.BLACK) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                addStartNode(board, color, distances, frontier, 0, col);
            }
        } else {
            for (int row = 0; row < BOARD_SIZE; row++) {
                addStartNode(board, color, distances, frontier, row, 0);
            }
        }

        while (!frontier.isEmpty()) {
            PathNode current = frontier.poll();
            if (current.cost != distances[current.row][current.col]) {
                continue;
            }

            if (reachesGoal(color, current.row, current.col)) {
                return new PathSearchResult(current.cost, reconstructPath(previous, current.row, current.col));
            }

            relaxOrthogonal(board, color, distances, previous, frontier, current);
            relaxDiagonal(board, color, distances, previous, frontier, current, -1, -1, current.row - 1, current.col - 1);
            relaxDiagonal(board, color, distances, previous, frontier, current, -1, 1, current.row - 1, current.col);
            relaxDiagonal(board, color, distances, previous, frontier, current, 1, -1, current.row, current.col - 1);
            relaxDiagonal(board, color, distances, previous, frontier, current, 1, 1, current.row, current.col);
        }

        return new PathSearchResult(BLOCKED, Collections.emptyList());
    }

    private void addStartNode(
            Board board,
            PlayerColor color,
            int[][] distances,
            PriorityQueue<PathNode> frontier,
            int row,
            int col
    ) {
        int cost = octCost(board, row, col, color);
        if (cost >= BLOCKED) {
            return;
        }

        distances[row][col] = cost;
        frontier.add(new PathNode(row, col, cost));
    }

    private void relaxOrthogonal(
            Board board,
            PlayerColor color,
            int[][] distances,
            PathStep[][] previous,
            PriorityQueue<PathNode> frontier,
            PathNode current
    ) {
        relaxOct(board, color, distances, previous, frontier, current, current.row - 1, current.col, false, -1, -1, 0);
        relaxOct(board, color, distances, previous, frontier, current, current.row + 1, current.col, false, -1, -1, 0);
        relaxOct(board, color, distances, previous, frontier, current, current.row, current.col - 1, false, -1, -1, 0);
        relaxOct(board, color, distances, previous, frontier, current, current.row, current.col + 1, false, -1, -1, 0);
    }

    private void relaxDiagonal(
            Board board,
            PlayerColor color,
            int[][] distances,
            PathStep[][] previous,
            PriorityQueue<PathNode> frontier,
            PathNode current,
            int rowDelta,
            int colDelta,
            int rhombRow,
            int rhombCol
    ) {
        if (!board.isRhombInBounds(rhombRow, rhombCol)) {
            return;
        }

        int edgeCost = rhombCost(board, rhombRow, rhombCol, color);
        if (edgeCost >= BLOCKED) {
            return;
        }

        relaxOct(
                board,
                color,
                distances,
                previous,
                frontier,
                current,
                current.row + rowDelta,
                current.col + colDelta,
                true,
                rhombRow,
                rhombCol,
                edgeCost
        );
    }

    private void relaxOct(
            Board board,
            PlayerColor color,
            int[][] distances,
            PathStep[][] previous,
            PriorityQueue<PathNode> frontier,
            PathNode current,
            int row,
            int col,
            boolean viaRhomb,
            int rhombRow,
            int rhombCol,
            int edgeCost
    ) {
        if (!board.isOctInBounds(row, col)) {
            return;
        }

        int octCost = octCost(board, row, col, color);
        if (octCost >= BLOCKED) {
            return;
        }

        int nextCost = current.cost + edgeCost + octCost;
        if (nextCost >= distances[row][col]) {
            return;
        }

        distances[row][col] = nextCost;
        previous[row][col] = new PathStep(current.row, current.col, viaRhomb, rhombRow, rhombCol);
        frontier.add(new PathNode(row, col, nextCost));
    }

    private List<String> reconstructPath(PathStep[][] previous, int goalRow, int goalCol) {
        List<String> reversedPath = new ArrayList<>();
        int row = goalRow;
        int col = goalCol;

        while (true) {
            reversedPath.add(idFor(CellType.OCT, row, col));
            PathStep step = previous[row][col];
            if (step == null) {
                break;
            }

            if (step.viaRhomb) {
                reversedPath.add(idFor(CellType.RHOMB, step.rhombRow, step.rhombCol));
            }

            row = step.previousRow;
            col = step.previousCol;
        }

        Collections.reverse(reversedPath);
        return reversedPath;
    }

    private int octCost(Board board, int row, int col, PlayerColor color) {
        PlayerColor occupant = board.getOct(row, col).getOccupant();
        if (occupant == color) {
            return 0;
        }

        if (occupant == null) {
            return 1;
        }

        return BLOCKED;
    }

    private int rhombCost(Board board, int row, int col, PlayerColor color) {
        PlayerColor occupant = board.getRhomb(row, col).getOccupant();
        if (occupant == color) {
            return 0;
        }

        if (occupant == null) {
            return 1;
        }

        return BLOCKED;
    }

    private boolean reachesGoal(PlayerColor color, int row, int col) {
        if (color == PlayerColor.BLACK) {
            return row == BOARD_SIZE - 1;
        }

        return col == BOARD_SIZE - 1;
    }

    private void apply(Board board, Move move) {
        if (move.isStoneMove()) {
            board.placeStone(move.getR(), move.getC(), move.getPlayer());
            return;
        }

        if (move.isTileMove()) {
            board.placeTile(move.getR(), move.getC(), move.getPlayer());
        }
    }

    private double centerDistance(Move move) {
        if (move.isStoneMove()) {
            return Math.abs(move.getR() - 5) + Math.abs(move.getC() - 5);
        }

        return Math.abs(move.getR() - 4.5) + Math.abs(move.getC() - 4.5);
    }

    private PlayerColor opponent(PlayerColor player) {
        return player == PlayerColor.BLACK ? PlayerColor.WHITE : PlayerColor.BLACK;
    }

    private String formatDistance(int distance) {
        if (distance >= BLOCKED) {
            return "blocked";
        }

        return String.valueOf(distance);
    }

    private String idFor(Move move) {
        return idFor(move.getCellType(), move.getR(), move.getC());
    }

    private String idFor(CellType cellType, int row, int col) {
        return cellType + ":" + row + ":" + col;
    }

    private static final class Evaluation {
        private final CandidateScore best;
        private final List<CandidateScore> rankedCandidates;

        private Evaluation(CandidateScore best, List<CandidateScore> rankedCandidates) {
            this.best = best;
            this.rankedCandidates = rankedCandidates;
        }
    }

    private static final class CandidateScore {
        private static final Comparator<CandidateScore> BEST_FIRST = new Comparator<CandidateScore>() {
            @Override
            public int compare(CandidateScore left, CandidateScore right) {
                int byScore = Double.compare(right.score, left.score);
                if (byScore != 0) {
                    return byScore;
                }

                int byOwnDistance = Integer.compare(left.ownDistance, right.ownDistance);
                if (byOwnDistance != 0) {
                    return byOwnDistance;
                }

                int byOpponentDistance = Integer.compare(right.opponentDistance, left.opponentDistance);
                if (byOpponentDistance != 0) {
                    return byOpponentDistance;
                }

                int byCenter = Double.compare(left.centerDistance, right.centerDistance);
                if (byCenter != 0) {
                    return byCenter;
                }

                int byType = left.move.getCellType().compareTo(right.move.getCellType());
                if (byType != 0) {
                    return byType;
                }

                int byRow = Integer.compare(left.move.getR(), right.move.getR());
                if (byRow != 0) {
                    return byRow;
                }

                return Integer.compare(left.move.getC(), right.move.getC());
            }
        };

        private final Move move;
        private final double score;
        private final int ownDistance;
        private final int opponentDistance;
        private final double centerDistance;
        private final List<String> pathIds;

        private CandidateScore(
                Move move,
                double score,
                int ownDistance,
                int opponentDistance,
                double centerDistance,
                List<String> pathIds
        ) {
            this.move = move;
            this.score = score;
            this.ownDistance = ownDistance;
            this.opponentDistance = opponentDistance;
            this.centerDistance = centerDistance;
            this.pathIds = pathIds;
        }
    }

    private static final class PathNode {
        private static final Comparator<PathNode> BEST_FIRST = new Comparator<PathNode>() {
            @Override
            public int compare(PathNode left, PathNode right) {
                int byCost = Integer.compare(left.cost, right.cost);
                if (byCost != 0) {
                    return byCost;
                }

                int byRow = Integer.compare(left.row, right.row);
                if (byRow != 0) {
                    return byRow;
                }

                return Integer.compare(left.col, right.col);
            }
        };

        private final int row;
        private final int col;
        private final int cost;

        private PathNode(int row, int col, int cost) {
            this.row = row;
            this.col = col;
            this.cost = cost;
        }
    }

    private static final class PathStep {
        private final int previousRow;
        private final int previousCol;
        private final boolean viaRhomb;
        private final int rhombRow;
        private final int rhombCol;

        private PathStep(int previousRow, int previousCol, boolean viaRhomb, int rhombRow, int rhombCol) {
            this.previousRow = previousRow;
            this.previousCol = previousCol;
            this.viaRhomb = viaRhomb;
            this.rhombRow = rhombRow;
            this.rhombCol = rhombCol;
        }
    }

    private static final class PathSearchResult {
        private final int cost;
        private final List<String> pathIds;

        private PathSearchResult(int cost, List<String> pathIds) {
            this.cost = cost;
            this.pathIds = pathIds;
        }
    }
}
