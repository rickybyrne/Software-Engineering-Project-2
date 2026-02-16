package quax.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BoardView {

    private final Pane root = new Pane();
    private boolean blackTurn = true;

    public BoardView() {
        drawBoard();
    }

    private void drawBoard() {

        int boardSize = 11;
        int cellSize = 50;

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {

                Rectangle rect = new Rectangle(
                        c * cellSize,
                        r * cellSize,
                        cellSize,
                        cellSize
                );

                rect.setFill(Color.ORANGE);
                rect.setStroke(Color.BLACK);

                rect.setOnMouseClicked(e -> {
                    if (rect.getFill().equals(Color.ORANGE)) {

                        if (blackTurn) {
                            rect.setFill(Color.BLACK);
                        } else {
                            rect.setFill(Color.WHITE);
                        }

                        blackTurn = !blackTurn;
                    }
                });

                root.getChildren().add(rect);
            }
        }
    }

    public Pane getRoot() {
        return root;
    }
}
