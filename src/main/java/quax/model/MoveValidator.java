package quax.model;

public class MoveValidator {

    public boolean validate(Move move, GameState state) {
        if (move == null || state == null || state.isGameOver()) {
            return false;
        }

        if (move.getPlayer() != state.getCurrentTurn()) {
            return false;
        }

        Board board = state.getBoard();

        if (move.isStoneMove()) {
            return board.isOctInBounds(move.getR(), move.getC()) && board.isOctEmpty(move.getR(), move.getC());
        }

        if (move.isTileMove()) {
            return board.isRhombInBounds(move.getR(), move.getC()) && board.isRhombEmpty(move.getR(), move.getC());
        }

        return false;
    }
}
