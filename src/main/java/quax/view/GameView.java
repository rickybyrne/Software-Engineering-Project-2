package quax.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
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
    private final Label devModeLabel;
    private final Button pieRuleButton;
    private final Button restartButton;
    private final Button moveOrderButton;
    private final Button moveListButton;
    private final TextArea moveListArea;
    private boolean winnerShown;
    private boolean showMoveOrder;
    private boolean showMoveList;
    private static final Font STATUS_FONT = Font.font("Arial", 16);
    private static final Font WINNER_FONT = Font.font("Arial", FontWeight.EXTRA_BOLD, 28);

    public GameView(GameController controller) {
        this.controller = controller;
        this.boardView = new BoardView();
        this.root = new BorderPane();

        this.titleLabel = new Label("Quax");
        this.modeLabel = new Label("Mode: Not selected");
        this.turnLabel = new Label("Current turn: BLACK");
        this.devModeLabel = new Label("DevMode: ON");
        this.pieRuleButton = new Button("Activate Pie Rule (claim opening move)");
        this.restartButton = new Button("Restart Game");
        this.moveOrderButton = new Button("Show Move Order On Board");
        this.moveListButton = new Button("Show Move List");
        this.moveListArea = new TextArea();
        this.winnerShown = false;
        this.showMoveOrder = false;
        this.showMoveList = false;

        buildLayout();
        connectBoardClicks();
        showModeSelection();
    }

    private void buildLayout() {

        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        modeLabel.setFont(STATUS_FONT);
        turnLabel.setFont(STATUS_FONT);
        devModeLabel.setFont(STATUS_FONT);

        pieRuleButton.setPrefWidth(240);
        pieRuleButton.setOnAction(e -> onPieRuleClicked());
        restartButton.setPrefWidth(160);
        restartButton.setOnAction(e -> onRestartClicked());
        moveOrderButton.setPrefWidth(160);
        moveOrderButton.setOnAction(e -> onMoveOrderClicked());
        moveListButton.setPrefWidth(160);
        moveListButton.setOnAction(e -> onMoveListClicked());
        moveListArea.setEditable(false);
        moveListArea.setWrapText(true);
        moveListArea.setPrefRowCount(14);
        moveListArea.setPrefColumnCount(26);

        // Hide it by default (important for the mode selection screen)
        pieRuleButton.setVisible(false);
        pieRuleButton.setManaged(false);
        restartButton.setVisible(false);
        restartButton.setManaged(false);
        devModeLabel.setVisible(false);
        devModeLabel.setManaged(false);
        moveOrderButton.setVisible(false);
        moveOrderButton.setManaged(false);
        moveListButton.setVisible(false);
        moveListButton.setManaged(false);
        moveListArea.setVisible(false);
        moveListArea.setManaged(false);

        VBox topPanel = new VBox(
                6,
                titleLabel,
                modeLabel,
                turnLabel,
                devModeLabel,
                moveOrderButton,
                moveListButton,
                moveListArea,
                restartButton,
                pieRuleButton
        );
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(16));

        root.setTop(topPanel);
        updateDevModeLabel();

        root.sceneProperty().addListener((obs, oldScene, newScene) -> installDevModeKeybind(newScene));
        if (root.getScene() != null) {
            installDevModeKeybind(root.getScene());
        }
    }

    private void installDevModeKeybind(Scene scene) {
        if (scene == null) {
            return;
        }

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.D) {
                controller.toggleDevMode();
                updateDevModeLabel();
                if (controller.getState() != null) {
                    render(controller.getState());
                }
            }
        });
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
        winnerShown = false;
        showMoveOrder = false;
        showMoveList = false;
        boardView.setShowMoveOrder(false, controller.getState());
        updateDevModeLabel();
        root.setCenter(boardView.getRoot());

        pieRuleButton.setVisible(false);
        pieRuleButton.setManaged(false);
        pieRuleButton.setDisable(true);
        restartButton.setVisible(true);
        restartButton.setManaged(true);
        restartButton.setDisable(false);

        render(controller.getState());
    }

    public void render(GameState state) {

        if (state == null) {
            return;
        }

        boardView.updateFrom(state);
        boardView.setShowMoveOrder(showMoveOrder, state);

        modeLabel.setText("Mode: " + asReadableMode(state.getMode()));
        turnLabel.setText(state.isGameOver()
                ? "Winner: " + state.getWinner()
                : "Current turn: " + state.getCurrentTurn());
        turnLabel.setFont(state.isGameOver() ? WINNER_FONT : STATUS_FONT);

        if (state.isGameOver() && state.getWinner() != null && !winnerShown) {
            winnerShown = true;
            showWinner(state.getWinner());
        }

        updateMoveList(state);

        boolean canPie = controller.canActivatePieRule();

        pieRuleButton.setVisible(canPie);
        pieRuleButton.setManaged(canPie);
        pieRuleButton.setDisable(!canPie);

        if (state.isGameOver()) {
            pieRuleButton.setVisible(false);
            pieRuleButton.setManaged(false);
        }

        if (controller.isDevModeEnabled()) {
            boardView.drawStrategyOverlay(controller.getLastBotOverlay());
        } else {
            boardView.clearStrategyOverlay();
        }
    }

    private void connectBoardClicks(){
        boardView.setOnOctClicked(this::onOctClicked, this::onOctRightClicked);
        boardView.setOnRhombClicked(this::onRhombClicked, this::onRhombRightClicked);

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

    public void onOctRightClicked(int r, int c) {
        if (controller.handleOctRightClick(r, c)) {
            render(controller.getState());
        }
    }

    public void onRhombRightClicked(int r, int c) {
        if (controller.handleRhombRightClick(r, c)) {
            render(controller.getState());
        }
    }

    public void onPieRuleClicked() {
        if (controller.activatePieRule()) {
            render(controller.getState());
        }
    }

    public void onRestartClicked() {
        if (controller.restartGame()) {
            winnerShown = false;
            showMoveOrder = false;
            showMoveList = false;
            boardView.setShowMoveOrder(false, controller.getState());
            updateDevModeLabel();
            render(controller.getState());
        }
    }

    public void onMoveOrderClicked() {
        if (!controller.isDevModeEnabled()) {
            return;
        }

        showMoveOrder = !showMoveOrder;
        boardView.setShowMoveOrder(showMoveOrder, controller.getState());
        updateMoveOrderButton();
    }

    public void onMoveListClicked() {
        if (!controller.isDevModeEnabled()) {
            return;
        }

        showMoveList = !showMoveList;
        updateMoveList(controller.getState());
        updateMoveListButton();
    }

    public void showWinner(PlayerColor winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Finished");
        alert.setHeaderText(null);
        alert.setContentText(winner + " wins!");
        alert.show();
    }

    private String asReadableMode(GameMode mode) {
        if (mode == GameMode.HUMAN_V_BOT) {
            return "Human vs Bot";
        }
        return "Human vs Human";
    }

    private void updateDevModeLabel() {
        boolean devModeEnabled = controller.isDevModeEnabled();
        devModeLabel.setVisible(devModeEnabled);
        devModeLabel.setManaged(devModeEnabled);
        devModeLabel.setText(devModeEnabled ? "DevMode: ON" : "");
        controller.toggleStrategyOverlay(devModeEnabled);

        if (!devModeEnabled) {
            showMoveOrder = false;
            showMoveList = false;
            boardView.setShowMoveOrder(false, controller.getState());
        }

        moveOrderButton.setVisible(devModeEnabled);
        moveOrderButton.setManaged(devModeEnabled);
        moveListButton.setVisible(devModeEnabled);
        moveListButton.setManaged(devModeEnabled);
        moveListArea.setVisible(devModeEnabled && showMoveList);
        moveListArea.setManaged(devModeEnabled && showMoveList);
        updateMoveOrderButton();
        updateMoveListButton();
        updateMoveList(controller.getState());
    }

    private void updateMoveOrderButton() {
        moveOrderButton.setText(showMoveOrder ? "Hide Move Order On Board" : "Show Move Order On Board");
    }

    private void updateMoveListButton() {
        moveListButton.setText(showMoveList ? "Hide Move List" : "Show Move List");
    }

    private void updateMoveList(GameState state) {
        if (!showMoveList || state == null) {
            moveListArea.clear();
            moveListArea.setVisible(false);
            moveListArea.setManaged(false);
            return;
        }

        moveListArea.setText(buildMoveList(state));
        moveListArea.setVisible(true);
        moveListArea.setManaged(true);
    }

    private String buildMoveList(GameState state) {
        String[] entries = new String[Math.max(0, state.getMoveCount())];

        for (int r = 0; r < 11; r++) {
            for (int c = 0; c < 11; c++) {
                int order = state.getBoard().getOct(r, c).getMoveOrder();
                if (order > 0) {
                    entries[order - 1] = order + ". "
                            + state.getBoard().getOct(r, c).getOccupant()
                            + " stone at "
                            + formatOctCoordinate(r, c);
                }
            }
        }

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                int order = state.getBoard().getRhomb(r, c).getMoveOrder();
                if (order > 0) {
                    entries[order - 1] = order + ". "
                            + state.getBoard().getRhomb(r, c).getOccupant()
                            + " tile at "
                            + formatRhombCoordinate(r, c);
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        for (String entry : entries) {
            if (entry == null) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(entry);
        }

        return builder.toString();
    }

    private String formatOctCoordinate(int r, int c) {
        return String.valueOf((char) ('A' + c)) + (11 - r);
    }

    private String formatRhombCoordinate(int r, int c) {
        char leftFile = (char) ('A' + c);
        char rightFile = (char) ('A' + c + 1);
        int topRow = 11 - r;
        int bottomRow = 10 - r;
        return leftFile + String.valueOf(topRow) + "-" + rightFile + bottomRow;
    }

    public Parent getRoot() {
        return root;
    }
}
