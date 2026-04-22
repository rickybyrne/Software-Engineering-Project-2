package quax.view;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import quax.controller.GameController;
import quax.model.GameMode;
import quax.model.GameState;
import quax.model.PlayerColor;
import quax.model.StrategyOverlay;

public class GameView {

    private static final double NARROW_LAYOUT_BREAKPOINT = 780;
    private static final Font STATUS_FONT = Font.font("Arial", 16);
    private static final Font WINNER_FONT = Font.font("Arial", FontWeight.EXTRA_BOLD, 28);

    private final GameController controller;
    private final BoardView boardView;

    private final BorderPane root;
    private final BorderPane gameArea;
    private final StackPane boardShell;
    private final ScrollPane sidePanelScroll;
    private final VBox sidePanel;
    private final VBox strategyNotesBox;
    private final VBox strategySection;

    private final Label titleLabel;
    private final Label modeLabel;
    private final Label turnLabel;
    private final Label devModeLabel;
    private final Button devModeButton;
    private final Button pieRuleButton;
    private final Button restartButton;
    private final Button moveOrderButton;
    private final Button moveListButton;
    private final TextArea moveListArea;
    private boolean winnerShown;
    private boolean showMoveOrder;
    private boolean showMoveList;
    private boolean gameStarted;

    public GameView(GameController controller) {
        this.controller = controller;
        this.boardView = new BoardView();
        this.root = new BorderPane();
        this.gameArea = new BorderPane();
        this.boardShell = new StackPane(boardView.getRoot());
        this.sidePanel = new VBox(10);
        this.sidePanelScroll = new ScrollPane(sidePanel);
        this.strategyNotesBox = new VBox(6);
        this.strategySection = new VBox(6);

        this.titleLabel = new Label("Quax");
        this.modeLabel = new Label("Mode: Not selected");
        this.turnLabel = new Label("Current turn: BLACK");
        this.devModeLabel = new Label("DevMode: ON");
        this.devModeButton = new Button("Enable Dev Mode");
        this.pieRuleButton = new Button("Activate Pie Rule (claim opening move)");
        this.restartButton = new Button("Restart Game");
        this.moveOrderButton = new Button("Show Move Order On Board");
        this.moveListButton = new Button("Show Move List");
        this.moveListArea = new TextArea();
        this.winnerShown = false;
        this.showMoveOrder = false;
        this.showMoveList = false;
        this.gameStarted = false;

        buildLayout();
        connectBoardClicks();
        showModeSelection();
    }

    private void buildLayout() {

        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        modeLabel.setFont(STATUS_FONT);
        turnLabel.setFont(STATUS_FONT);
        devModeLabel.setFont(STATUS_FONT);

        devModeButton.setOnAction(e -> toggleDevMode());
        pieRuleButton.setOnAction(e -> onPieRuleClicked());
        restartButton.setOnAction(e -> onRestartClicked());
        moveOrderButton.setOnAction(e -> onMoveOrderClicked());
        moveListButton.setOnAction(e -> onMoveListClicked());

        for (Button button : List.of(devModeButton, pieRuleButton, restartButton, moveOrderButton, moveListButton)) {
            button.setMaxWidth(Double.MAX_VALUE);
        }

        pieRuleButton.setPrefWidth(260);
        moveListArea.setEditable(false);
        moveListArea.setWrapText(true);
        moveListArea.setPrefRowCount(12);
        moveListArea.setPrefColumnCount(24);
        moveListArea.setMaxWidth(Double.MAX_VALUE);

        Label strategyTitle = new Label("Bot analysis");
        strategyTitle.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 16));
        strategySection.getChildren().setAll(strategyTitle, strategyNotesBox);
        strategySection.setVisible(false);
        strategySection.setManaged(false);

        boardShell.setAlignment(Pos.CENTER);
        boardShell.setPadding(new Insets(12, 18, 18, 18));
        boardShell.setMinSize(0, 0);
        boardShell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        sidePanel.getChildren().setAll(
                restartButton,
                pieRuleButton,
                devModeButton,
                devModeLabel,
                moveOrderButton,
                moveListButton,
                moveListArea,
                strategySection
        );
        sidePanel.setAlignment(Pos.TOP_LEFT);
        sidePanel.setFillWidth(true);
        sidePanel.setPadding(new Insets(14));
        sidePanel.setPrefWidth(280);

        sidePanelScroll.setContent(sidePanel);
        sidePanelScroll.setFitToWidth(true);
        sidePanelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidePanelScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidePanelScroll.setPannable(true);
        sidePanelScroll.setVisible(false);
        sidePanelScroll.setManaged(false);
        sidePanelScroll.setPrefWidth(280);
        sidePanelScroll.setMinWidth(240);
        sidePanelScroll.setMaxWidth(320);

        gameArea.setCenter(boardShell);

        VBox topPanel = new VBox(4, titleLabel, modeLabel, turnLabel);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(14, 18, 10, 18));

        root.setTop(topPanel);
        updateDevModeLabel();

        root.widthProperty().addListener((obs, oldValue, newValue) -> updateResponsiveGameLayout());
        root.heightProperty().addListener((obs, oldValue, newValue) -> updateResponsiveGameLayout());
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
                toggleDevMode();
            }
        });
    }

    private void showModeSelection() {

        gameStarted = false;

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
        modeSelection.setPadding(new Insets(24));

        root.setCenter(modeSelection);
    }

    private void startGame(GameMode mode) {
        controller.newGame(mode);
        winnerShown = false;
        showMoveOrder = false;
        showMoveList = false;
        gameStarted = true;
        boardView.setShowMoveOrder(false, controller.getState());

        sidePanelScroll.setVisible(true);
        sidePanelScroll.setManaged(true);
        restartButton.setVisible(true);
        restartButton.setManaged(true);
        restartButton.setDisable(false);

        updateDevModeLabel();
        root.setCenter(gameArea);
        updateResponsiveGameLayout();

        pieRuleButton.setVisible(false);
        pieRuleButton.setManaged(false);
        pieRuleButton.setDisable(true);

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

        StrategyOverlay overlay = controller.getLastBotOverlay();
        if (controller.isDevModeEnabled()) {
            boardView.drawStrategyOverlay(overlay);
            updateStrategyNotes(overlay);
        } else {
            boardView.clearStrategyOverlay();
            updateStrategyNotes(null);
        }
    }

    private void connectBoardClicks() {
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
        controller.toggleStrategyOverlay(devModeEnabled);

        devModeButton.setVisible(gameStarted);
        devModeButton.setManaged(gameStarted);
        devModeButton.setText(devModeEnabled ? "Disable Dev Mode" : "Enable Dev Mode");

        devModeLabel.setVisible(gameStarted && devModeEnabled);
        devModeLabel.setManaged(gameStarted && devModeEnabled);
        devModeLabel.setText(devModeEnabled ? "DevMode: ON" : "");

        if (!devModeEnabled) {
            showMoveOrder = false;
            showMoveList = false;
            boardView.setShowMoveOrder(false, controller.getState());
            updateStrategyNotes(null);
        }

        moveOrderButton.setVisible(gameStarted && devModeEnabled);
        moveOrderButton.setManaged(gameStarted && devModeEnabled);
        moveListButton.setVisible(gameStarted && devModeEnabled);
        moveListButton.setManaged(gameStarted && devModeEnabled);
        moveListArea.setVisible(gameStarted && devModeEnabled && showMoveList);
        moveListArea.setManaged(gameStarted && devModeEnabled && showMoveList);
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
        if (!showMoveList || state == null || !controller.isDevModeEnabled()) {
            moveListArea.clear();
            moveListArea.setVisible(false);
            moveListArea.setManaged(false);
            return;
        }

        moveListArea.setText(buildMoveList(state));
        moveListArea.setVisible(true);
        moveListArea.setManaged(true);
    }

    private void updateStrategyNotes(StrategyOverlay overlay) {
        strategyNotesBox.getChildren().clear();

        if (!controller.isDevModeEnabled() || overlay == null || overlay.getNotes() == null || overlay.getNotes().isEmpty()) {
            strategySection.setVisible(false);
            strategySection.setManaged(false);
            return;
        }

        for (String note : overlay.getNotes()) {
            Label noteLabel = new Label(note);
            noteLabel.getStyleClass().add("strategy-note");
            noteLabel.setWrapText(true);
            strategyNotesBox.getChildren().add(noteLabel);
        }

        strategySection.setVisible(true);
        strategySection.setManaged(true);
    }

    private void updateResponsiveGameLayout() {
        if (!gameStarted) {
            return;
        }

        if (root.getWidth() > 0 && root.getWidth() < NARROW_LAYOUT_BREAKPOINT) {
            gameArea.setRight(null);
            gameArea.setBottom(sidePanelScroll);
            BorderPane.setMargin(sidePanelScroll, new Insets(0, 18, 18, 18));
            sidePanelScroll.setMinWidth(0);
            sidePanelScroll.setMaxWidth(Double.MAX_VALUE);
            double controlsHeight = root.getHeight() > 0
                    ? Math.max(170, Math.min(240, root.getHeight() * 0.30))
                    : 200;
            sidePanelScroll.setPrefHeight(controlsHeight);
        } else {
            gameArea.setBottom(null);
            gameArea.setRight(sidePanelScroll);
            BorderPane.setMargin(sidePanelScroll, new Insets(12, 18, 18, 0));
            sidePanelScroll.setPrefWidth(280);
            sidePanelScroll.setMinWidth(240);
            sidePanelScroll.setMaxWidth(320);
            sidePanelScroll.setPrefHeight(Region.USE_COMPUTED_SIZE);
        }
    }

    private void toggleDevMode() {
        controller.toggleDevMode();
        updateDevModeLabel();
        if (controller.getState() != null) {
            render(controller.getState());
        }
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
