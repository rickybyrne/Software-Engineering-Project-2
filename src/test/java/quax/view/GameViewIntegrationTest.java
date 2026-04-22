package quax.view;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import quax.controller.GameController;
import quax.testutil.FxTestHelper;

class GameViewIntegrationTest {

    @BeforeAll
    static void setupFx() {
        FxTestHelper.initToolkit();
    }

    @Test
    void launchScreenShowsTitleModePromptAndDefaultTurn() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();

            Label title = findLabelByText(root, "Quax");
            Label prompt = findLabelByText(root, "Select game mode");
            Label turn = findLabelByText(root, "Current turn: BLACK");
            Label devMode = findLabelByText(root, "DevMode: ON");
            Button moveOrder = findButtonByText(root, "Show Move Order On Board");
            Button moveList = findButtonByText(root, "Show Move List");

            assertNotNull(title);
            assertNotNull(prompt);
            assertNotNull(turn);
            assertTrue(devMode == null || !devMode.isVisible());
            assertTrue(moveOrder == null || !moveOrder.isVisible());
            assertTrue(moveList == null || !moveList.isVisible());
        });
    }

    @Test
    void launchScreenShowsBothModeButtons() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();

            Button hvh = findButtonByText(root, "Human vs Human");
            Button hvb = findButtonByText(root, "Human vs Bot");

            assertNotNull(hvh);
            assertNotNull(hvb);
        });
    }

    @Test
    void selectingHumanVsBotUpdatesModeLabel() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();

            Button hvb = findButtonByText(root, "Human vs Bot");
            assertNotNull(hvb);

            hvb.fire();

            Label modeLabel = findLabelByText(root, "Mode: Human vs Bot");
            Label turnLabel = findLabelByText(root, "Current turn: WHITE");
            Button pieRuleButton = findButtonByText(root, "Activate Pie Rule (claim opening move)");
            Button restartButton = findButtonByText(root, "Restart Game");

            assertNotNull(modeLabel);
            assertNotNull(turnLabel);
            assertNotNull(pieRuleButton);
            assertNotNull(restartButton);
            assertTrue(pieRuleButton.isVisible());
            assertTrue(restartButton.isVisible());
        });
    }

    @Test
    void selectingHumanVsHumanUpdatesModeLabel() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();

            Button hvh = findButtonByText(root, "Human vs Human");
            assertNotNull(hvh);

            hvh.fire();

            Label modeLabel = findLabelByText(root, "Mode: Human vs Human");
            assertNotNull(modeLabel);
        });
    }

    @Test
    void boardLayoutContainsExpectedNumberOfPlayableShapes() {
        FxTestHelper.runOnFxThread(() -> {
            BoardView boardView = new BoardView();

            int polygonCount = countAllPolygons(boardView.getRoot());
            // 11x11 octagons + 10x10 rhombs
            assertEquals(221, polygonCount);
        });
    }

    @Test
    void firstMoveUpdatesTurnLabelAndEnablesPieRuleButton() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();

            Button hvh = findButtonByText(root, "Human vs Human");
            assertNotNull(hvh);
            hvh.fire();

            Button pieRuleButton = findButtonByText(root, "Activate Pie Rule (claim opening move)");
            assertNotNull(pieRuleButton);
            assertFalse(pieRuleButton.isVisible());

            view.onOctClicked(0, 0);

            Label whiteTurn = findLabelByText(root, "Current turn: WHITE");
            assertNotNull(whiteTurn);
            assertTrue(pieRuleButton.isVisible());
            assertFalse(pieRuleButton.isDisable());
        });
    }

    @Test
    void activatingPieRuleUpdatesTurnAndDisablesPieRuleOption() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();

            Button hvh = findButtonByText(root, "Human vs Human");
            assertNotNull(hvh);
            hvh.fire();

            view.onOctClicked(0, 0);

            Button pieRuleButton = findButtonByText(root, "Activate Pie Rule (claim opening move)");
            assertNotNull(pieRuleButton);
            assertTrue(pieRuleButton.isVisible());

            pieRuleButton.fire();

            Label blackTurn = findLabelByText(root, "Current turn: BLACK");
            assertNotNull(blackTurn);
            assertFalse(pieRuleButton.isVisible());
            assertTrue(pieRuleButton.isDisable());
        });
    }

    @Test
    void winningMoveUpdatesTurnLabelToWinnerMessage() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();

            Button hvh = findButtonByText(root, "Human vs Human");
            assertNotNull(hvh);
            hvh.fire();

            for (int row = 0; row < 10; row++) {
                view.onOctClicked(row, 0);
                view.onOctClicked(row, 1);
            }
            view.onOctClicked(10, 0);

            Label winnerLabel = findLabelByText(root, "Winner: BLACK");
            assertNotNull(winnerLabel);
        });
    }

    @Test
    void devModeKeybindTogglesLabelAndRightClickHandlersRespectTurn() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();
            Scene scene = new Scene(root, 860, 680);

            Button hvh = findButtonByText(root, "Human vs Human");
            assertNotNull(hvh);
            hvh.fire();

            Label devModeOff = findLabelByText(root, "DevMode: ON");
            assertTrue(devModeOff == null || !devModeOff.isVisible());

            view.onOctRightClicked(0, 0);
            Label stillBlackTurn = findLabelByText(root, "Current turn: BLACK");
            assertNotNull(stillBlackTurn);

            scene.getOnKeyPressed().handle(new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "d",
                    "d",
                    KeyCode.D,
                    false,
                    false,
                    false,
                    false
            ));

            Label devModeOn = findLabelByText(root, "DevMode: ON");
            assertNotNull(devModeOn);
            assertTrue(devModeOn.isVisible());

            view.onOctRightClicked(0, 0);
            Label blackTurnAfterEdit = findLabelByText(root, "Current turn: BLACK");
            assertNotNull(blackTurnAfterEdit);

            view.onOctClicked(0, 1);
            Label whiteTurn = findLabelByText(root, "Current turn: WHITE");
            assertNotNull(whiteTurn);
        });
    }

    @Test
    void devModeMoveOrderButtonTogglesMoveOrderLabels() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();
            Scene scene = new Scene(root, 860, 680);

            Button hvh = findButtonByText(root, "Human vs Human");
            assertNotNull(hvh);
            hvh.fire();

            view.onOctClicked(0, 0);
            view.onRhombClicked(0, 0);

            Button hiddenMoveOrderButton = findButtonByText(root, "Show Move Order On Board");
            assertNotNull(hiddenMoveOrderButton);
            assertFalse(hiddenMoveOrderButton.isVisible());
            assertEquals(0, countMoveOrderLabels(root));

            scene.getOnKeyPressed().handle(new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "d",
                    "d",
                    KeyCode.D,
                    false,
                    false,
                    false,
                    false
            ));

            Button showMoveOrderButton = findButtonByText(root, "Show Move Order On Board");
            assertNotNull(showMoveOrderButton);
            assertTrue(showMoveOrderButton.isVisible());

            showMoveOrderButton.fire();

            Button hideMoveOrderButton = findButtonByText(root, "Hide Move Order On Board");
            assertNotNull(hideMoveOrderButton);
            assertTrue(countMoveOrderLabels(root) >= 2);

            hideMoveOrderButton.fire();

            assertEquals(0, countMoveOrderLabels(root));
        });
    }

    @Test
    void botOverlayAppearsOnlyWhenDevModeIsEnabled() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();
            Scene scene = new Scene(root, 860, 680);

            Button hvb = findButtonByText(root, "Human vs Bot");
            assertNotNull(hvb);
            hvb.fire();

            assertTrue(findByStyleClass(root, "strategy-heat").isEmpty());
            assertTrue(findByStyleClass(root, "strategy-path").isEmpty());

            scene.getOnKeyPressed().handle(new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "d",
                    "d",
                    KeyCode.D,
                    false,
                    false,
                    false,
                    false
            ));

            assertFalse(findByStyleClass(root, "strategy-heat").isEmpty());
            assertFalse(findByStyleClass(root, "strategy-path").isEmpty());
            assertFalse(findByStyleClass(root, "strategy-note").isEmpty());
            assertNotNull(findButtonByText(root, "Show Move Order On Board"));
            assertNotNull(findButtonByText(root, "Show Move List"));

            scene.getOnKeyPressed().handle(new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "d",
                    "d",
                    KeyCode.D,
                    false,
                    false,
                    false,
                    false
            ));

            assertTrue(findByStyleClass(root, "strategy-heat").isEmpty());
            assertTrue(findByStyleClass(root, "strategy-path").isEmpty());
        });
    }

    @Test
    void devModeMoveListButtonShowsReadablePlacementOrder() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();
            Scene scene = new Scene(root, 860, 680);

            Button hvh = findButtonByText(root, "Human vs Human");
            assertNotNull(hvh);
            hvh.fire();

            view.onOctClicked(0, 0);
            view.onRhombClicked(0, 0);

            Button hiddenMoveListButton = findButtonByText(root, "Show Move List");
            assertNotNull(hiddenMoveListButton);
            assertFalse(hiddenMoveListButton.isVisible());

            scene.getOnKeyPressed().handle(new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "d",
                    "d",
                    KeyCode.D,
                    false,
                    false,
                    false,
                    false
            ));

            Button showMoveListButton = findButtonByText(root, "Show Move List");
            assertNotNull(showMoveListButton);
            assertTrue(showMoveListButton.isVisible());

            showMoveListButton.fire();

            TextArea moveListArea = findTextArea(root);
            assertNotNull(moveListArea);
            assertTrue(moveListArea.isVisible());
            assertTrue(moveListArea.getText().contains("1. BLACK stone at A11"));
            assertTrue(moveListArea.getText().contains("2. WHITE tile at A11-B10"));
        });
    }

    @Test
    void restartButtonResetsCurrentGameState() {
        FxTestHelper.runOnFxThread(() -> {
            GameView view = new GameView(new GameController());
            Parent root = view.getRoot();

            Button hvh = findButtonByText(root, "Human vs Human");
            assertNotNull(hvh);
            hvh.fire();

            view.onOctClicked(0, 0);
            view.onRhombClicked(0, 0);

            Button restartButton = findButtonByText(root, "Restart Game");
            assertNotNull(restartButton);
            restartButton.fire();

            Label blackTurn = findLabelByText(root, "Current turn: BLACK");
            Label modeLabel = findLabelByText(root, "Mode: Human vs Human");

            assertNotNull(blackTurn);
            assertNotNull(modeLabel);
            assertEquals(0, countMoveOrderLabels(root));
        });
    }

    private Label findLabelByText(Parent root, String text) {
        List<Label> labels = findAll(root, Label.class);
        for (Label label : labels) {
            if (text.equals(label.getText())) {
                return label;
            }
        }
        return null;
    }

    private Button findButtonByText(Parent root, String text) {
        List<Button> buttons = findAll(root, Button.class);
        for (Button button : buttons) {
            if (text.equals(button.getText())) {
                return button;
            }
        }
        return null;
    }

    private int countAllPolygons(Parent root) {
        return findAll(root, javafx.scene.shape.Polygon.class).size();
    }

    private List<Node> findByStyleClass(Parent root, String styleClass) {
        List<Node> matches = new ArrayList<>();
        for (Node node : findAll(root, Node.class)) {
            if (node.getStyleClass().contains(styleClass)) {
                matches.add(node);
            }
        }
        return matches;
    }

    private TextArea findTextArea(Parent root) {
        List<TextArea> textAreas = findAll(root, TextArea.class);
        return textAreas.isEmpty() ? null : textAreas.get(0);
    }

    private int countMoveOrderLabels(Parent root) {
        int count = 0;
        for (javafx.scene.text.Text text : findAll(root, javafx.scene.text.Text.class)) {
            if (text.getStyleClass().contains("move-order-label")) {
                count++;
            }
        }
        return count;
    }

    private <T extends javafx.scene.Node> List<T> findAll(Parent root, Class<T> type) {
        List<T> matches = new ArrayList<>();
        walk(root, type, matches);
        return matches;
    }

    private <T extends javafx.scene.Node> void walk(javafx.scene.Node node, Class<T> type, List<T> out) {
        if (type.isInstance(node)) {
            out.add(type.cast(node));
        }
        if (node instanceof Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                walk(child, type, out);
            }
        }
    }
}
