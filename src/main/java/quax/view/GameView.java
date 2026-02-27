package quax.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import quax.controller.GameController;
import quax.model.GameMode;
import quax.model.GameState;
import quax.model.PlayerColor;

public class GameView {

    private final GameController controller;
    private final BoardView boardView;

    private final BorderPane root;

    private final Label titleLabel;
    private final Label modeLabel;
    private final Label turnLabel;

    public GameView(GameController controller) {
        this.controller = controller;
        this.boardView = new BoardView();
        this.root = new BorderPane();

        this.titleLabel = new Label("Quax");
        this.modeLabel = new Label("Mode: Not selected");
        this.turnLabel = new Label("Current turn: BLACK");

        buildLayout();
        connectBoardClicks();
        showModeSelection();
    }

    private void buildLayout() {

        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        modeLabel.setFont(Font.font("Arial", 16));
        turnLabel.setFont(Font.font("Arial", 16));

        VBox topPanel = new VBox(6, titleLabel, modeLabel, turnLabel);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(16));

        root.setTop(topPanel);
    }

    private void showModeSelection() {

        Label prompt = new Label("Select game mode");
        prompt.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 20));

        Button humanVsHumanButton = new Button("Human vs Human");
        humanVsHumanButton.setPrefWidth(220);
        humanVsHumanButton.setOnAction(event -> startGame(GameMode.HUMAN_V_HUMAN));

        Button humanVsBotButton = new Button("Human vs Bot");
        humanVsBotButton.setPrefWidth(220);
        humanVsBotButton.setOnAction(event -> startGame(GameMode.HUMAN_V_BOT));

        VBox modeSelection = new VBox(14, prompt, humanVsHumanButton, humanVsBotButton);
        modeSelection.setAlignment(Pos.CENTER);

        root.setCenter(modeSelection);
    }

    private void startGame(GameMode mode) {
        controller.newGame(mode);
        root.setCenter(boardView.getRoot());
        render(controller.getState());
    }

    public void render(GameState state) {

        if (state == null) {
            return;
        }

        boardView.updateFrom(state);

        modeLabel.setText("Mode: " + asReadableMode(state.getMode()));
        turnLabel.setText("Current turn: " + state.getCurrentTurn());

        if (state.isGameOver() && state.getWinner() != null) {
            showWinner(state.getWinner());
        }
    }

    private void connectBoardClicks(){
        boardView.setOnOctClicked(this::onOctClicked);
        boardView.setOnRhombClicked(this::onRhombClicked);

    }

    public void onOctClicked(int r, int c) {
        if (controller.handleOctClick(r, c)) {
            render(controller.getState());
        }
    }

    public void onRhombClicked(int r, int c) {
        if (controller.handleRhombClick(r, c)) {
            render(controller.getState());
        }
    }

    public void onPieRuleClicked() {
        if (controller.activatePieRule()) {
            render(controller.getState());
        }
    }

    public void onShowStrategy() {
        controller.toggleStrategyOverlay(true);
    }

    public void onHideStrategy() {
        controller.toggleStrategyOverlay(false);
        boardView.clearStrategyOverlay();
    }

    public void showWinner(PlayerColor winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Finished");
        alert.setHeaderText(null);
        alert.setContentText("Winner: " + winner);
        alert.showAndWait();
    }

    private String asReadableMode(GameMode mode) {
        if (mode == GameMode.HUMAN_V_BOT) {
            return "Human vs Bot";
        }
        return "Human vs Human";
    }

    public Parent getRoot() {
        return root;
    }
}
