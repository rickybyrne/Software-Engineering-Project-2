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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

            assertNotNull(title);
            assertNotNull(prompt);
            assertNotNull(turn);
            assertTrue(devMode == null || !devMode.isVisible());
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

            assertNotNull(modeLabel);
            assertNotNull(turnLabel);
            assertNotNull(pieRuleButton);
            assertTrue(pieRuleButton.isVisible());
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
