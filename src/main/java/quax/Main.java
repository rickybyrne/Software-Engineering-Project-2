package quax;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import quax.controller.GameController;
import quax.view.GameView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        GameController controller = new GameController();
        GameView gameView = new GameView(controller);

        Scene scene = new Scene(gameView.getRoot(), 860, 680);

        stage.setTitle("Quax");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
