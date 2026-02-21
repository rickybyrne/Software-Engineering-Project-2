package quax.view;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import quax.controller.GameController;
import quax.testutil.FxTestHelper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

            assertNotNull(title);
            assertNotNull(prompt);
            assertNotNull(turn);
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
            Label turnLabel = findLabelByText(root, "Current turn: BLACK");

            assertNotNull(modeLabel);
            assertNotNull(turnLabel);
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
