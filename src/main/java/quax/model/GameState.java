package quax.model;

public class GameState {

    private static final class OpeningMove {
        private final CellType cellType;
        private final int row;
        private final int col;
        private final int moveOrder;

        private OpeningMove(CellType cellType, int row, int col, int moveOrder) {
            this.cellType = cellType;
            this.row = row;
            this.col = col;
            this.moveOrder = moveOrder;
        }
    }

    private PlayerColor currentTurn;
    private boolean gameOver;
    private PlayerColor winner;
    private boolean pieRuleAvailable;
    private final GameMode mode;
    private int moveCount;
    private final Board board;
    private final Player[] players;
    private OpeningMove openingMove;

    public GameState(GameMode mode) {
        this.mode = mode;
        this.board = new Board();
        this.currentTurn = PlayerColor.BLACK;
        this.gameOver = false;
        this.winner = null;
        this.pieRuleAvailable = true;
        this.moveCount = 0;
        this.openingMove = null;

        PlayerKind blackKind = mode == GameMode.HUMAN_V_BOT ? PlayerKind.BOT : PlayerKind.HUMAN;
        Player blackPlayer = new Player(PlayerColor.BLACK, blackKind);
        Player whitePlayer = new Player(PlayerColor.WHITE, PlayerKind.HUMAN);
        this.players = new Player[]{blackPlayer, whitePlayer};
    }//getter. gets cell at r c index, will be used in future

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean canUsePieRule() {
        return pieRuleAvailable && moveCount == 1 && currentTurn == PlayerColor.WHITE;
    }

    public void apply(Move move) {
        if (move == null || gameOver) {
            return;
        }

        int appliedMoveOrder = moveCount + 1;

        if (moveCount == 0) {
            openingMove = new OpeningMove(
                    move.getCellType(),
                    move.getR(),
                    move.getC(),
                    appliedMoveOrder
            );
        }

        if (move.isStoneMove()) {
            board.placeStone(move.getR(), move.getC(), move.getPlayer(), appliedMoveOrder);
        } else if (move.isTileMove()) {
            board.placeTile(move.getR(), move.getC(), move.getPlayer(), appliedMoveOrder);
        }

        moveCount++;

        if (moveCount > 1) {
            pieRuleAvailable = false;
        }

        toggleTurn();
    }

    public void toggleTurn() {
        currentTurn = currentTurn == PlayerColor.BLACK ? PlayerColor.WHITE : PlayerColor.BLACK;
    }

    public void endGame(PlayerColor winner) {
        this.winner = winner;
        this.gameOver = true;
    }

    public void disablePieRule() {
        this.pieRuleAvailable = false;
    }

    public void claimOpeningMoveForWhite() {
        if (!canUsePieRule() || openingMove == null) {
            return;
        }

        recolourOpeningPiece(PlayerColor.WHITE);
        currentTurn = PlayerColor.BLACK;
        disablePieRule();
    }

    private void recolourOpeningPiece(PlayerColor color) {
        if (openingMove.cellType == CellType.OCT) {
            board.placeStone(openingMove.row, openingMove.col, color, openingMove.moveOrder);
            return;
        }

        if (openingMove.cellType == CellType.RHOMB) {
            board.placeTile(openingMove.row, openingMove.col, color, openingMove.moveOrder);
        }
    }

    public Player getPlayerForColor(PlayerColor color) {
        for (Player player : players) {
            if (player.getColor() == color) {
                return player;
            }
        }

        return null;
    }

    public boolean isCurrentTurnBot() {
        Player player = getPlayerForColor(currentTurn);
        return player != null && player.getKind() == PlayerKind.BOT;
    }

    public boolean isCurrentTurnHuman() {
        Player player = getPlayerForColor(currentTurn);
        return player != null && player.getKind() == PlayerKind.HUMAN;
    }

    public PlayerColor getCurrentTurn() {
        return currentTurn;
    }

    public PlayerColor getWinner() {
        return winner;
    }

    public boolean isPieRuleAvailable() {
        return pieRuleAvailable;
    }

    public GameMode getMode() {
        return mode;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public Board getBoard() {
        return board;
    }

    public Player[] getPlayers() {
        return players;
    }
}
