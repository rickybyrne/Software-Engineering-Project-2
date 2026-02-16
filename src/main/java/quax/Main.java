package quax;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    private boolean blackTurn = true;

    @Override
    public void start(Stage stage) {

        Pane root = new Pane();
        int cellSize = 50;
        int boardSize = 11;

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) { //MAKES 11X11 BOARD

                Rectangle rect = new Rectangle(  //MAKING RECTANGLES AND GETTING THE SIZE
                        c * cellSize,
                        r * cellSize,
                        cellSize,
                        cellSize
                );

                rect.setFill(Color.ORANGE);
                rect.setStroke(Color.BLACK);

                rect.setOnMouseClicked(e -> {
                    // Only allow move if cell is empty
                    if (rect.getFill().equals(Color.ORANGE)) {

                        if (blackTurn) {
                            rect.setFill(Color.BLACK);
                        } else {
                            rect.setFill(Color.WHITE);
                        }

                        blackTurn = !blackTurn; // switch turn
                    }
                });

                root.getChildren().add(rect);
            }
        }

        Scene scene = new Scene(root, boardSize * cellSize, boardSize * cellSize);

        stage.setTitle("Quax Game");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
