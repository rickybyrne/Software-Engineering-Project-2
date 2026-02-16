package quax;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import quax.view.BoardView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        BoardView boardView = new BoardView();

        Scene scene = new Scene(boardView.getRoot(), 600, 600);

        stage.setTitle("Quax Sprint 1");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
