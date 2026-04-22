package quax.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class ModuleBotStrategy implements BotStrategy {

    private static final int BOARD_SIZE = 11;
    private static final int RHOMB_SIZE = 10;
    private static final int OPENING_ROW = 5;
    private static final int OPENING_COL = 6;
    private static final int BLOCKED = 1_000_000;

    private static final int[][] MODULE_DIRECTIONS = {
            {1, 0},
            {0, 1},
            {1, 1},
            {1, -1}
    };

    private static final int[][] ADJACENT_DIRECTIONS = {
            {-1, -1},
            {-1, 0},
            {-1, 1},
            {0, -1},
            {0, 1},
            {1, -1},
            {1, 0},
            {1, 1}
    };

    private final WinDetector winDetector = new WinDetector();

    @Override
    public Move selectMove(GameState state) {
        Choice choice = chooseMove(state);
        return choice == null ? null : choice.move;
    }

    @Override
    public StrategyOverlay explain(GameState state) {
        Choice choice = chooseMove(state);
        Map<String, Double> weights = new LinkedHashMap<>();
        List<String> notes = new ArrayList<>();

        if (choice == null) {
            notes.add("No legal bot move is available.");
            return new StrategyOverlay(weights, notes);
        }

        notes.add("Selected " + label(choice.move) + ": " + choice.reason + ".");

        int limit = Math.min(5, choice.rankedMoves.size());
        for (int i = 0; i < limit; i++) {
            MoveScore score = choice.rankedMoves.get(i);
            weights.put(label(score.move), score.score);
        }

        return new StrategyOverlay(weights, notes);
    }

    private Choice chooseMove(GameState state) {
        if (state == null || state.isGameOver()) {
            return null;
        }

        Board board = state.getBoard();
        PlayerColor player = state.getCurrentTurn();
        List<Move> legalMoves = legalMoves(board, player);
        if (legalMoves.isEmpty()) {
            return null;
        }

        Move openingMove = openingMove(state, legalMoves);
        if (openingMove != null) {
            return singleMoveChoice(openingMove, 10_000, "opening book");
        }

        Move winningMove = findWinningMove(board, legalMoves, player);
        if (winningMove != null) {
            return singleMoveChoice(winningMove, 9_000, "winning move");
        }

        Move blockingMove = findImmediateBlock(board, player);
        if (blockingMove != null) {
            return singleMoveChoice(blockingMove, 8_000, "blocking immediate win");
        }

        List<MoveScore> rankedMoves = rankMoves(board, legalMoves, player);
        if (rankedMoves.isEmpty()) {
            return null;
        }

        MoveScore best = rankedMoves.get(0);
        return new Choice(best.move, best.score, best.reason, rankedMoves);
    }

    private Choice singleMoveChoice(Move move, double score, String reason) {
        List<MoveScore> rankedMoves = new ArrayList<>();
        rankedMoves.add(new MoveScore(move, score, reason));
        return new Choice(move, score, reason, rankedMoves);
    }

    private Move openingMove(GameState state, List<Move> legalMoves) {
        if (state.getMoveCount() != 0 || state.getCurrentTurn() != PlayerColor.BLACK) {
            return null;
        }

        Board board = state.getBoard();
        if (board.isOctEmpty(OPENING_ROW, OPENING_COL)) {
            return new Move(CellType.OCT, OPENING_ROW, OPENING_COL, PlayerColor.BLACK);
        }

        Move bestFallback = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Move move : legalMoves) {
            if (!move.isStoneMove()) {
                continue;
            }

            int distance = Math.abs(move.getR() - OPENING_ROW) + Math.abs(move.getC() - OPENING_COL);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestFallback = move;
            }
        }

        return bestFallback;
    }

    private Move findWinningMove(Board board, List<Move> legalMoves, PlayerColor player) {
        for (Move move : legalMoves) {
            if (winsAfter(board, move, player)) {
                return move;
            }
        }

        return null;
    }

    private Move findImmediateBlock(Board board, PlayerColor player) {
        PlayerColor opponent = opponent(player);
        List<Move> opponentMoves = legalMoves(board, opponent);

        for (Move opponentMove : opponentMoves) {
            if (winsAfter(board, opponentMove, opponent)) {
                return new Move(opponentMove.getCellType(), opponentMove.getR(), opponentMove.getC(), player);
            }
        }

        return null;
    }

    private boolean winsAfter(Board board, Move move, PlayerColor player) {
        Board copy = board.copy();
        apply(copy, move);
        return winDetector.checkWinner(copy) == player;
    }

    private List<MoveScore> rankMoves(Board board, List<Move> legalMoves, PlayerColor player) {
        PlayerColor opponent = opponent(player);
        int ownPathBefore = shortestPathCost(board, player);
        int opponentPathBefore = shortestPathCost(board, opponent);
        List<MoveScore> rankedMoves = new ArrayList<>();

        for (Move move : legalMoves) {
            Board copy = board.copy();
            apply(copy, move);

            int ownPathAfter = shortestPathCost(copy, player);
            int opponentPathAfter = shortestPathCost(copy, opponent);

            double ownPathGain = pathGain(ownPathBefore, ownPathAfter) * 80;
            double opponentDelay = pathDelay(opponentPathBefore, opponentPathAfter) * 65;
            double module = moduleScore(board, move, player);
            double repair = repairScore(board, move, player);
            double bridge = bridgeScore(board, move, player);
            double bridgeBlock = bridgeBlockScore(board, move, player);
            double center = centerScore(move);
            double stoneBias = move.isStoneMove() ? 4 : 0;

            double score = ownPathGain + opponentDelay + module + repair + bridge + bridgeBlock + center + stoneBias;
            String reason = reasonFor(ownPathGain, opponentDelay, module, repair, bridge, bridgeBlock);

            rankedMoves.add(new MoveScore(move, score, reason));
        }

        rankedMoves.sort(MoveScore.BEST_FIRST);
        return rankedMoves;
    }

    private double pathGain(int before, int after) {
        if (before >= BLOCKED && after >= BLOCKED) {
            return 0;
        }
        if (before >= BLOCKED) {
            return 5;
        }
        if (after >= BLOCKED) {
            return 8;
        }
        return before - after;
    }

    private double pathDelay(int before, int after) {
        if (before >= BLOCKED && after >= BLOCKED) {
            return 0;
        }
        if (after >= BLOCKED) {
            return 8;
        }
        if (before >= BLOCKED) {
            return 0;
        }
        return after - before;
    }

    private String reasonFor(
            double ownPathGain,
            double opponentDelay,
            double module,
            double repair,
            double bridge,
            double bridgeBlock
    ) {
        if (bridge >= 170) {
            return "building bridge chain";
        }
        if (bridgeBlock >= 150) {
            return "cutting bridge threat";
        }
        if (module >= 90) {
            return "building module";
        }
        if (repair >= 45) {
            return "repairing module";
        }
        if (opponentDelay > ownPathGain && opponentDelay > 0) {
            return "blocking white path";
        }
        return "path progress";
    }

    private double moduleScore(Board board, Move move, PlayerColor player) {
        if (move.isStoneMove()) {
            return stoneModuleScore(board, move, player);
        }

        if (move.isTileMove()) {
            return rhombModuleScore(board, move, player);
        }

        return 0;
    }

    private double stoneModuleScore(Board board, Move move, PlayerColor player) {
        double score = 0;
        int row = move.getR();
        int col = move.getC();

        for (int[] direction : MODULE_DIRECTIONS) {
            int dr = direction[0];
            int dc = direction[1];
            double weight = primaryDirectionWeight(player, dr, dc);

            if (ownsOct(board, row - dr, col - dc, player) && ownsOct(board, row + dr, col + dc, player)) {
                score += 220 * weight;
            }

            if (ownsOct(board, row + 2 * dr, col + 2 * dc, player)
                    && isEmptyOct(board, row + dr, col + dc)) {
                score += 55 * weight;
            }

            if (ownsOct(board, row - 2 * dr, col - 2 * dc, player)
                    && isEmptyOct(board, row - dr, col - dc)) {
                score += 55 * weight;
            }

            if (ownsOct(board, row + dr, col + dc, player)) {
                score += 18 * weight;
            }

            if (ownsOct(board, row - dr, col - dc, player)) {
                score += 18 * weight;
            }
        }

        return score;
    }

    private double bridgeScore(Board board, Move move, PlayerColor player) {
        if (move.isTileMove()) {
            return tileBridgeScore(board, move, player);
        }

        if (move.isStoneMove()) {
            return stoneBridgeScore(board, move, player);
        }

        return 0;
    }

    private double tileBridgeScore(Board board, Move move, PlayerColor player) {
        int row = move.getR();
        int col = move.getC();
        double score = 0;

        score += bridgePairScore(board, row, col, row + 1, col + 1, player);
        score += bridgePairScore(board, row, col + 1, row + 1, col, player);

        return score;
    }

    private double stoneBridgeScore(Board board, Move move, PlayerColor player) {
        int row = move.getR();
        int col = move.getC();
        double score = 0;

        score += stoneBridgeDirectionScore(board, row, col, row - 1, col - 1, row - 1, col - 1, player);
        score += stoneBridgeDirectionScore(board, row, col, row - 1, col + 1, row - 1, col, player);
        score += stoneBridgeDirectionScore(board, row, col, row + 1, col - 1, row, col - 1, player);
        score += stoneBridgeDirectionScore(board, row, col, row + 1, col + 1, row, col, player);

        return score;
    }

    private double stoneBridgeDirectionScore(
            Board board,
            int row,
            int col,
            int targetRow,
            int targetCol,
            int rhombRow,
            int rhombCol,
            PlayerColor player
    ) {
        if (!board.isOctInBounds(targetRow, targetCol)) {
            return 0;
        }

        boolean targetOwned = ownsOct(board, targetRow, targetCol, player);
        if (!targetOwned) {
            return 0;
        }

        if (board.isRhombInBounds(rhombRow, rhombCol) && board.getRhomb(rhombRow, rhombCol).getOccupant() == player) {
            return 185;
        }

        if (board.isRhombInBounds(rhombRow, rhombCol) && board.getRhomb(rhombRow, rhombCol).isEmpty()) {
            return 52;
        }

        return 0;
    }

    private double bridgePairScore(
            Board board,
            int rowA,
            int colA,
            int rowB,
            int colB,
            PlayerColor player
    ) {
        if (!board.isOctInBounds(rowA, colA) || !board.isOctInBounds(rowB, colB)) {
            return 0;
        }

        boolean aOwned = ownsOct(board, rowA, colA, player);
        boolean bOwned = ownsOct(board, rowB, colB, player);
        boolean aEmpty = isEmptyOct(board, rowA, colA);
        boolean bEmpty = isEmptyOct(board, rowB, colB);

        if (aOwned && bOwned) {
            return 250;
        }

        if ((aOwned && bEmpty) || (bOwned && aEmpty)) {
            return 38;
        }

        return 0;
    }

    private double bridgeBlockScore(Board board, Move move, PlayerColor player) {
        PlayerColor opponent = opponent(player);

        if (move.isTileMove()) {
            int row = move.getR();
            int col = move.getC();
            return blockingBridgePairScore(board, row, col, row + 1, col + 1, opponent)
                    + blockingBridgePairScore(board, row, col + 1, row + 1, col, opponent);
        }

        if (move.isStoneMove()) {
            int row = move.getR();
            int col = move.getC();
            return blockingStoneBridgeScore(board, row, col, row - 1, col - 1, row - 1, col - 1, opponent)
                    + blockingStoneBridgeScore(board, row, col, row - 1, col + 1, row - 1, col, opponent)
                    + blockingStoneBridgeScore(board, row, col, row + 1, col - 1, row, col - 1, opponent)
                    + blockingStoneBridgeScore(board, row, col, row + 1, col + 1, row, col, opponent);
        }

        return 0;
    }

    private double blockingBridgePairScore(
            Board board,
            int rowA,
            int colA,
            int rowB,
            int colB,
            PlayerColor opponent
    ) {
        if (!board.isOctInBounds(rowA, colA) || !board.isOctInBounds(rowB, colB)) {
            return 0;
        }

        boolean aOwned = ownsOct(board, rowA, colA, opponent);
        boolean bOwned = ownsOct(board, rowB, colB, opponent);
        boolean aEmpty = isEmptyOct(board, rowA, colA);
        boolean bEmpty = isEmptyOct(board, rowB, colB);

        if (aOwned && bOwned) {
            return 205;
        }

        if ((aOwned && bEmpty) || (bOwned && aEmpty)) {
            return 26;
        }

        return 0;
    }

    private double blockingStoneBridgeScore(
            Board board,
            int row,
            int col,
            int targetRow,
            int targetCol,
            int rhombRow,
            int rhombCol,
            PlayerColor opponent
    ) {
        if (!board.isOctInBounds(targetRow, targetCol) || !ownsOct(board, targetRow, targetCol, opponent)) {
            return 0;
        }

        if (board.isRhombInBounds(rhombRow, rhombCol) && board.getRhomb(rhombRow, rhombCol).getOccupant() == opponent) {
            return 175;
        }

        if (board.isRhombInBounds(rhombRow, rhombCol) && board.getRhomb(rhombRow, rhombCol).isEmpty()) {
            return 24;
        }

        return 0;
    }

    private double rhombModuleScore(Board board, Move move, PlayerColor player) {
        int row = move.getR();
        int col = move.getC();
        double score = 0;

        score += diagonalPairScore(board, row, col, row + 1, col + 1, player);
        score += diagonalPairScore(board, row, col + 1, row + 1, col, player);

        return score;
    }

    private double diagonalPairScore(
            Board board,
            int rowA,
            int colA,
            int rowB,
            int colB,
            PlayerColor player
    ) {
        if (!board.isOctInBounds(rowA, colA) || !board.isOctInBounds(rowB, colB)) {
            return 0;
        }

        boolean aOwned = ownsOct(board, rowA, colA, player);
        boolean bOwned = ownsOct(board, rowB, colB, player);
        boolean aEmpty = isEmptyOct(board, rowA, colA);
        boolean bEmpty = isEmptyOct(board, rowB, colB);

        if (aOwned && bOwned) {
            return 125;
        }

        if ((aOwned && bEmpty) || (bOwned && aEmpty)) {
            return 28;
        }

        return 0;
    }

    private double repairScore(Board board, Move move, PlayerColor player) {
        PlayerColor opponent = opponent(player);

        if (move.isStoneMove()) {
            int ownNeighbours = 0;
            int opponentNeighbours = 0;

            for (int[] direction : ADJACENT_DIRECTIONS) {
                int row = move.getR() + direction[0];
                int col = move.getC() + direction[1];
                if (!board.isOctInBounds(row, col)) {
                    continue;
                }

                PlayerColor occupant = board.getOct(row, col).getOccupant();
                if (occupant == player) {
                    ownNeighbours++;
                } else if (occupant == opponent) {
                    opponentNeighbours++;
                }
            }

            return ownNeighbours * opponentNeighbours * 18 + (opponentNeighbours >= 2 ? 25 : 0);
        }

        int ownCorners = 0;
        int opponentCorners = 0;
        int[][] corners = {
                {move.getR(), move.getC()},
                {move.getR(), move.getC() + 1},
                {move.getR() + 1, move.getC()},
                {move.getR() + 1, move.getC() + 1}
        };

        for (int[] corner : corners) {
            if (!board.isOctInBounds(corner[0], corner[1])) {
                continue;
            }

            PlayerColor occupant = board.getOct(corner[0], corner[1]).getOccupant();
            if (occupant == player) {
                ownCorners++;
            } else if (occupant == opponent) {
                opponentCorners++;
            }
        }

        return ownCorners * opponentCorners * 12;
    }

    private double primaryDirectionWeight(PlayerColor player, int dr, int dc) {
        if (player == PlayerColor.BLACK && dc == 0) {
            return 1.35;
        }

        if (player == PlayerColor.WHITE && dr == 0) {
            return 1.35;
        }

        return 1.0;
    }

    private double centerScore(Move move) {
        if (move.isStoneMove()) {
            int distance = Math.abs(move.getR() - 5) + Math.abs(move.getC() - 5);
            return Math.max(0, 14 - distance);
        }

        double rowDistance = Math.abs(move.getR() - 4.5);
        double colDistance = Math.abs(move.getC() - 4.5);
        return Math.max(0, 9 - rowDistance - colDistance);
    }

    private int shortestPathCost(Board board, PlayerColor color) {
        int[][] distances = new int[BOARD_SIZE][BOARD_SIZE];
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
                return current.cost;
            }

            relaxOrthogonal(board, color, distances, frontier, current);
            relaxDiagonal(board, color, distances, frontier, current, -1, -1, current.row - 1, current.col - 1);
            relaxDiagonal(board, color, distances, frontier, current, -1, 1, current.row - 1, current.col);
            relaxDiagonal(board, color, distances, frontier, current, 1, -1, current.row, current.col - 1);
            relaxDiagonal(board, color, distances, frontier, current, 1, 1, current.row, current.col);
        }

        return BLOCKED;
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
            PriorityQueue<PathNode> frontier,
            PathNode current
    ) {
        relaxOct(board, color, distances, frontier, current, current.row - 1, current.col, 0);
        relaxOct(board, color, distances, frontier, current, current.row + 1, current.col, 0);
        relaxOct(board, color, distances, frontier, current, current.row, current.col - 1, 0);
        relaxOct(board, color, distances, frontier, current, current.row, current.col + 1, 0);
    }

    private void relaxDiagonal(
            Board board,
            PlayerColor color,
            int[][] distances,
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
                frontier,
                current,
                current.row + rowDelta,
                current.col + colDelta,
                edgeCost
        );
    }

    private void relaxOct(
            Board board,
            PlayerColor color,
            int[][] distances,
            PriorityQueue<PathNode> frontier,
            PathNode current,
            int row,
            int col,
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
        frontier.add(new PathNode(row, col, nextCost));
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

    private List<Move> legalMoves(Board board, PlayerColor player) {
        List<Move> moves = new ArrayList<>();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board.isOctEmpty(row, col)) {
                    moves.add(new Move(CellType.OCT, row, col, player));
                }
            }
        }

        for (int row = 0; row < RHOMB_SIZE; row++) {
            for (int col = 0; col < RHOMB_SIZE; col++) {
                if (board.isRhombEmpty(row, col)) {
                    moves.add(new Move(CellType.RHOMB, row, col, player));
                }
            }
        }

        return moves;
    }

    private boolean ownsOct(Board board, int row, int col, PlayerColor color) {
        return board.isOctInBounds(row, col) && board.getOct(row, col).getOccupant() == color;
    }

    private boolean isEmptyOct(Board board, int row, int col) {
        return board.isOctInBounds(row, col) && board.getOct(row, col).isEmpty();
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

    private PlayerColor opponent(PlayerColor player) {
        return player == PlayerColor.BLACK ? PlayerColor.WHITE : PlayerColor.BLACK;
    }

    private String label(Move move) {
        return move.getCellType() + "(" + move.getR() + "," + move.getC() + ")";
    }

    private static final class Choice {
        private final Move move;
        private final double score;
        private final String reason;
        private final List<MoveScore> rankedMoves;

        private Choice(Move move, double score, String reason, List<MoveScore> rankedMoves) {
            this.move = move;
            this.score = score;
            this.reason = reason;
            this.rankedMoves = rankedMoves;
        }
    }

    private static final class MoveScore {
        private static final Comparator<MoveScore> BEST_FIRST = new Comparator<MoveScore>() {
            @Override
            public int compare(MoveScore left, MoveScore right) {
                int byScore = Double.compare(right.score, left.score);
                if (byScore != 0) {
                    return byScore;
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
        private final String reason;

        private MoveScore(Move move, double score, String reason) {
            this.move = move;
            this.score = score;
            this.reason = reason;
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
}
