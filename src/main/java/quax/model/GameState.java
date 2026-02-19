package quax.model;

public class GameState {

    private PlayerColor currentTurn;
    private boolean gameOver;
    private PlayerColor winner;
    private boolean pieRuleAvailable;
    private final GameMode mode;
    private int moveCount;
    private final Board board;
    private final Player[] players;

    public GameState(GameMode mode) {
        this.mode = mode;
        this.board = new Board();
        this.currentTurn = PlayerColor.BLACK;
        this.gameOver = false;
        this.winner = null;
        this.pieRuleAvailable = true;
        this.moveCount = 0;

        Player blackPlayer = new Player(PlayerColor.BLACK, PlayerKind.HUMAN);
        PlayerKind whiteKind = mode == GameMode.HUMAN_V_BOT ? PlayerKind.BOT : PlayerKind.HUMAN;
        Player whitePlayer = new Player(PlayerColor.WHITE, whiteKind);
        this.players = new Player[]{blackPlayer, whitePlayer};
    }

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

        if (move.isStoneMove()) {
            board.placeStone(move.getR(), move.getC(), move.getPlayer());
        } else if (move.isTileMove()) {
            board.placeTile(move.getR(), move.getC(), move.getPlayer());
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

    public void swapPlayerColours() {
        Player blackPlayer = findPlayer(PlayerColor.BLACK);
        Player whitePlayer = findPlayer(PlayerColor.WHITE);

        if (blackPlayer != null) {
            blackPlayer.setColor(PlayerColor.WHITE);
        }

        if (whitePlayer != null) {
            whitePlayer.setColor(PlayerColor.BLACK);
        }

        toggleTurn();
    }

    private Player findPlayer(PlayerColor color) {
        for (Player player : players) {
            if (player.getColor() == color) {
                return player;
            }
        }

        return null;
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
