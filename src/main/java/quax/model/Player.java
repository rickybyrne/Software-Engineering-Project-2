package quax.model;

public class Player {

    private PlayerColor color;
    private final PlayerKind kind;

    public Player(PlayerColor color, PlayerKind kind) {
        this.color = color;
        this.kind = kind;
    }

    public PlayerColor getColor() {
        return color;
    }

    public void setColor(PlayerColor color) {
        this.color = color;
    }

    public PlayerKind getKind() {
        return kind;
    }
}
